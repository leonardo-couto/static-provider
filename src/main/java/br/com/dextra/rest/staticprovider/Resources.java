package br.com.dextra.rest.staticprovider;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Iterator;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("")
public class Resources {

    private String basePath = ServerProperties.property("source.path");

    @GET
    @Path("{path:.*}")
    public Response doGet(@PathParam("path") String path) {
        if (path == null || path.isEmpty()) {
            URI location = this.index();
            return (location == null) ? this.send404() : Response.seeOther(location).build();
        }
        
        File f = new File(basePath + "/" + path);

        if (f == null || !f.exists() || !f.isFile()) {
            return this.send404();
        }

        String mime = this.getContentType(f);
        return Response.ok(f, mime).build();
    }

    /**
     * @return the name of the first index file found on webapp root directory
     */
    private URI index() {
        java.nio.file.Path dir = FileSystems.getDefault().getPath(this.basePath);
        try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(dir, "index.*")) {
            Iterator<java.nio.file.Path> iterator = stream.iterator();

            if (iterator.hasNext()) {
                java.nio.file.Path index = iterator.next();
                String location = index.getFileName().toString();
                return new URI(location);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private Response send404() {
        File f = new File(basePath + "/404.html");
        ResponseBuilder response = Response.status(Response.Status.NOT_FOUND);
        return f.isFile() ? response.entity(f).build() : response.build();
    }

    private String getContentType(File f) {
        String contentType = null;
        try {
            contentType = Files.probeContentType(f.toPath());
        } catch (IOException e) {
            // do nothing
        }

        String mime = contentType == null ? (new MimetypesFileTypeMap()).getContentType(f) : contentType;
        return mime;
    }

}
