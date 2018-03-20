package controllers;

import java.io.IOException;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;

/**
 *
 * @author jacobotapia
 */
@SessionScoped
@ManagedBean
public class IndexController implements Serializable {
    
    public void showLFSRPage() throws IOException {
        String contextPath = FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
        FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath + "/faces/views/lfsr/index.xhtml");
    }
    
}
