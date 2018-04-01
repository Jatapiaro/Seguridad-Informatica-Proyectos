package controllers;

import java.io.IOException;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;

/**
 *
 * Proyectos - Seguridad Informática
 * Autor: Jacobo Misael Tapia de la Rosa
 * Matricula: 1336590   Carrera: ITC-11
 * Correo Electronico: A01336590@itesm.mx
 * Fecha de creacion: 18-mar-2018
 * Fecha última modificiacion: 20-mar-2018
 * Nombre Archivo: IndexController.java
 * Archivos relacionados: LfsrController.java
 * Ejecucion: Acceder a https://seguridad-informatica.herokuapp.com/
 * Plataforma: Navegadores Chrome, Safari, Firefox e Internet Explorer
 * Descripción: Programa para redirigir a la ruta necesaria
 * @author jacobotapia
 */
@SessionScoped
@ManagedBean
public class IndexController implements Serializable {
    
    /**
    * Este método unicamente se utiliza para
    * redireccionar a la página donde está 
    * el simulador LFSR
    */
    public void showLFSRPage() throws IOException {
        String contextPath = FacesContext.getCurrentInstance().getExternalContext().getApplicationContextPath();
        FacesContext.getCurrentInstance().getExternalContext().redirect(contextPath + "/faces/views/lfsr/index.xhtml");
    }
    
}
