package br.com.dextra.rest.staticprovider;

public class Redirect {
    
    private String source; 
    private String target; 
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public boolean isValid() {
        return (source != null && target != null);
    }
}
