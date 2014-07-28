package br.com.dextra.rest.staticprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Iterator;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/")
public class Resources extends HttpServlet {

	private static final long serialVersionUID = -3695914304133163599L;
	private String basePath = ServerProperties.property("source.path");
    private String sourceJs = ServerProperties.property("javascript.source.path");
    private String targetJs = ServerProperties.property("javascript.target.path");
    private boolean redirectJs = (!sourceJs.isEmpty() && !targetJs.isEmpty());


    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	
    	String path = req.getServletPath();
    	
        if (path == null || path.isEmpty() || "/".equals(path)) {
            URI location = this.index();
            if (location == null) {
            	resp.sendError(404);
            } else {
            	resp.sendRedirect(location.toString());
            }
            
            return;
        }
        
        File f = null;
        if (this.redirectJs && path.startsWith("/" + targetJs)) {
        	String jsPath = path.substring(targetJs.length() + 2);
        	f = new File(this.sourceJs + "/" + jsPath);
        	
        } else {
        	f = new File(this.basePath + "/" + path);
        }

        if (f == null || !f.exists() || !f.isFile()) {
        	resp.sendError(404);
        	return;
        }

        String mime = this.getContentType(f);
        
        resp.setContentType(mime);
        resp.setStatus(200);
        ServletOutputStream os = resp.getOutputStream();
        
        FileInputStream fileInput = new FileInputStream(f);
        byte[] buffer = new byte[10240];
        
        int read = fileInput.read(buffer);
        while (read > -1) {
        	os.write(buffer, 0, read);
        	read = fileInput.read(buffer);
        }
        
        fileInput.close();
        resp.flushBuffer();
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

    private String getContentType(File f) {
        String contentType = null;
        if (hasFileExtension(f, "css")) {
        	return "text/css";
        }
        
        try {
            contentType = Files.probeContentType(f.toPath());
        } catch (IOException e) {
            // do nothing
        }

        String mime = contentType == null ? (new MimetypesFileTypeMap()).getContentType(f) : contentType;
        return mime;
    }
    
    private boolean hasFileExtension(File f, String extension) {
    	return f.getPath().endsWith(extension);
    }
    
    @Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doGet(req, resp);
	}

}
