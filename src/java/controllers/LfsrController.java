/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import com.lfsr.LFSR;
import com.lfsr.Step;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import org.primefaces.PrimeFaces;
import org.primefaces.event.diagram.ConnectEvent;
import org.primefaces.event.diagram.DisconnectEvent;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.FlowChartConnector;
import org.primefaces.model.diagram.endpoint.BlankEndPoint;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.endpoint.RectangleEndPoint;

 /*
 * Proyectos - Seguridad Informática
 * Autor: Jacobo Misael Tapia de la Rosa
 * Matricula: 1336590   Carrera: ITC-11
 * Correo Electronico: A01336590@itesm.mx
 * Fecha de creacion: 18-mar-2018
 * Fecha última modificiacion: 20-mar-2018
 * Nombre Archivo: LfsrController.java
 * Archivos relacionados: IndexController.java
 * Ejecucion: Acceder a https://seguridad-informatica.herokuapp.com/
 * Plataforma: Navegadores Chrome, Safari, Firefox e Internet Explorer
 * Descripción: Programa para redirigir a la ruta necesaria
 * @author jacobotapia
 */
@SessionScoped
@ManagedBean
public class LfsrController implements Serializable {

    /**
     * @return the steps
     */
    public List<Step> getSteps() {
        return steps;
    }

    /*
    * @param String binarySeed la semilla en binario
    * @param long seed la semilla representada como un entero, se representó con un long
    *           para que podamos validar que no pase de Integer.MAX_VALUE
    * @param LFSR lfsr objeto de la librería LFSR que hace el proceso del simulador
    * @param String rowsPerTemplate número de filas que puede tener la tabla de 
                resultados por página
    * @param model El modelo o digrama donde hacemos la simulación
    * @param List<Integer> connections connections la lista de conexiones que 
                que tenemos con el xor. Es decir los taps
    * @param boolean endProcess variable que nos indica si el lfsr ha terminado su simualación.
    * @param List<Step> steps lista de todos los pasos de la simulación
                la cual termina cuando se repite la semilla
    * @param int stepIndex en que paso vamos a la hora de recorrer la simulación
    */
    private String binarySeed;
    private long seed;
    
    private LFSR lfsr;
    
    private String rowsPerTemplate = "10, 50, 100";
    
    private DefaultDiagramModel model;
    private List<Integer> connections;
    
    private boolean endProcess = false;
    
    private List<Step> steps;
    private int stepIndex = 0;
    
    
    /**
    * Inicializa la parte visual y los elementos del simulador 
    */
    @PostConstruct
    public void init() {
        
        this.binarySeed = "0001";
        this.seed = 1;
        this.lfsr = new LFSR(binarySeed);
        this.connections = new ArrayList<Integer>();
        this.endProcess = false;
        
        initModel();
        
    }
    
    /**
    * Inicia el modelo o grafo donde se mostrará
    * la simulación.
    */
    private void initModel() {
        
        /*
        * Se define el diagrama
        * y como se va a ver asi como el tipo de conexiones que tendrá
        */
        this.model = new DefaultDiagramModel();
        this.model.setMaxConnections(1000);
        FlowChartConnector connector = new FlowChartConnector();
        connector.setPaintStyle("{strokeStyle:'#C7B097',lineWidth:3}");
        this.model.setDefaultConnector(connector);
        
        /*
        * Se definen las variables para mantener en una posicion definida
        * la semilla binaria y el resultado de la simulación o de los pasos
        * del corrimiento
        */
        int startX = 100;
        int step = 30;
        int startY = 200;
        boolean first = true;
        int id = 0;
        /*
        * Para cada elemento de la semilla
        * creamos un elemento en el grafo para poderlo conectar con el xor
        */
        for ( char c :  this.binarySeed.toCharArray() ) {
            Element el = new Element(""+c, "5em", "1.5em");
            el.setDraggable(false);
            el.setX(startX+"px");
            el.setY(startY+"px");
            startX += step;
            if ( first == true ) {
                EndPoint endPoint = createRectangleEndPoint(EndPointAnchor.LEFT, false);
                endPoint.setTarget(false);
                endPoint.setSource(true);
                first = false;
                el.setId(""+id);
                id++;
                el.addEndPoint(endPoint);
            } else {
               EndPoint endPoint = createDotEndPoint(EndPointAnchor.TOP, true); 
               endPoint.setTarget(false);
               endPoint.setSource(true);
               el.setId(""+id);
               id++;
               el.addEndPoint(endPoint);
            }
            this.model.addElement(el);
        }
        
        /*
        * Se agrega el nodo que representa el xor
        * y agregamos los conectores por defecto del
        * nodo más a la derecha al xor
        */
        Element xor = new Element("xor", "5em", "1.5em");
        xor.setDraggable(false);
        xor.setX((startX/2)+"px");
        xor.setY("60px");
        xor.setStyleClass("ui-diagram-circle");
        EndPoint endPoint = createRectangleEndPoint(EndPointAnchor.BOTTOM, false); 
        endPoint.setSource(false);
        endPoint.setTarget(true);
        endPoint.setMaxConnections(1000);
        xor.addEndPoint(endPoint);
        this.model.addElement(xor);
        
        Element ele = model.getElements().get(0);
        Connection conn = new Connection(ele.getEndPoints().get(0), xor.getEndPoints().get(0));
        this.model.connect(conn);         
        
    }
    
    /**
    * Llamado por ajax cada vez que se actualiza la semilla
    * De ser así, se reinician todos los parametros y taps
    */
    public void updateSeed() {
        this.reset();
        this.connections = new ArrayList<Integer>();
        System.out.println(seed);
        this.getLfsr().setIntialSeed((int)this.seed);
        this.binarySeed = getLfsr().getInitialBinarySeed();
        this.initModel();
    }
    
    /**
    * Llamado por ajax cada vez que se actualiza la semilla
    * De ser así, se reinician todos los parametros y taps
    */
    public void updateBinarySeed() {
        this.reset();
        this.connections = new ArrayList<Integer>();
        this.getLfsr().setInitialBinarySeed(this.binarySeed);
        this.seed = this.getLfsr().getIntialSeed();
        this.initModel();
    }
    
    /**
    * Llamado cuando se presiona el botón de procesar
    */
    public void makeProcess() {
        //En caso de tener una semilla vacia o taps vacios enviar un error
        //Caso contrario ejecutar el proceso
        if ( this.binarySeed.length() <= 0 ) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Necesitas ingresar una semilla."));
        }
        if ( this.lfsr.getTaps().size() == 0 ) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", "Necesita haber al menos un tap seleccionado. Por favor, conecta un recuadro del gráfico inferior con el xor, utilizando los conectores circulares verdes."));
            return;
        }
        this.makeProcess(true);
    }
    
    /**
    * Se hace el proceso del lfsr
    * Y se asignan las variables para ser desplegado en la vista
    */
    private void makeProcess(boolean b) {
        this.steps = new ArrayList<Step>();
        this.stepIndex = -1;
        this.lfsr.makeProcess();
        this.steps = this.lfsr.getStepList();
        this.rowsPerTemplate = this.lfsr.getStepList().size() + ", 10, 50, 100";
        this.stepIndex = 0;
        this.initModel2();
        this.endProcess = true;
    }
    
    /*
    * Vuelve a dibujar el grafo con la diferencia de que
    * ahora se muestra una fila con el resultado para poder simular 
    * y ver el corrimiento en cada step
    */
    private void initModel2() {
        
        
        Step stepO = this.getSteps().get(stepIndex);
        
        this.model = new DefaultDiagramModel();
        this.model.setMaxConnections(1000);
        FlowChartConnector connector = new FlowChartConnector();
        connector.setPaintStyle("{strokeStyle:'#C7B097',lineWidth:3}");
        this.model.setDefaultConnector(connector);
        
        
        int startX = 100;
        int step = 30;
        int startY = 200;
        boolean first = true;
        int id = 0;
        for ( char c :  stepO.getBinarySeed().toCharArray() ) {
            Element el = new Element(""+c, "5em", "1.5em");
            el.setDraggable(false);
            el.setX(startX+"px");
            el.setY(startY+"px");
            
            startX += step;
            if ( first == true ) {
                el.setStyleClass("ui-diagram-fail");
                EndPoint endPoint = createRectangleEndPoint(EndPointAnchor.LEFT, false);
                endPoint.setTarget(false);
                endPoint.setSource(true);
                first = false;
                el.setId(""+id);
                id++;
                el.addEndPoint(endPoint);
            } else {
               EndPoint endPoint = createDotEndPoint(EndPointAnchor.TOP, true); 
               endPoint.setTarget(false);
               endPoint.setSource(true);
               el.setId(""+id);
               id++;
               el.addEndPoint(endPoint);
            }
            this.model.addElement(el);
        }
        
        int startX2 = 100;
        startY = 300;
        int le = stepO.getNewBinarySeed().toCharArray().length - 1;
        int counter2 = 0;
        for ( char c :  stepO.getNewBinarySeed().toCharArray() ) {
            Element el2 = new Element(""+c, "5em", "1.5em");
            el2.setDraggable(false);
            el2.setX(startX2+"px");
            el2.setY(startY+"px");
            startX2 += step;
            el2.setId(""+id);
            id++;
            EndPoint endPoint = createRectangleEndPoint2(EndPointAnchor.RIGHT, false);
            endPoint.setTarget(false);
            endPoint.setSource(true);
            el2.addEndPoint(endPoint);
            if ( counter2 == le ) {
                el2.setStyleClass("ui-diagram-fail");
                EndPoint endPoint2 = createRectangleEndPoint(EndPointAnchor.RIGHT, false);
                endPoint2.setTarget(true);
                endPoint2.setSource(false);
                el2.addEndPoint(endPoint2);
            }
            counter2++;
            model.addElement(el2);
        }
        
       
        Element xor = new Element("xor", "5em", "1.5em");
        xor.setId("xor");
        xor.setDraggable(false);
        xor.setX((startX/2)+"px");
        xor.setY("60px");
        xor.setStyleClass("ui-diagram-circle");
        EndPoint endPoint = createRectangleEndPoint(EndPointAnchor.BOTTOM, false); 
        endPoint.setSource(false);
        endPoint.setTarget(true);
        endPoint.setMaxConnections(1000);
        xor.addEndPoint(endPoint);
        EndPoint endPointX = createDotEndPoint(EndPointAnchor.RIGHT, false); 
        endPointX.setSource(true);
        endPointX.setTarget(false);
        xor.addEndPoint(endPointX);
        this.model.addElement(xor);
        
        Element ele = model.getElements().get(0);
        Connection conn = new Connection(ele.getEndPoints().get(0), xor.getEndPoints().get(0));
        this.model.connect(conn);    
        
        Element ele2 = model.getElements().get(model.getElements().size()-2);
        /*Connection conn2 = new Connection(ele2.getEndPoints().get(1), xor.getEndPoints().get(1));
        this.model.connect(conn2);*/
        
        this.addPreviousConnections();
        
        StringBuilder sb = new StringBuilder();
        sb.append(stepO.getOutBit()+"^");
        for ( int i = 0; i<stepO.getTaps().size(); i++ ) {
            if ( i < stepO.getTaps().size()-1 ) {
                sb.append(stepO.getTaps().get(i)+"^");
            } else {
                sb.append(stepO.getTaps().get(i));
            }
        }
        sb.append(" = " +stepO.getNewBinarySeed().charAt(stepO.getNewBinarySeed().length()-1));
        
        Element res = new Element(sb.toString(), "5em", "1.5em");
        res.setStyleClass("big");
        res.setId("res");
        res.setDraggable(false);
        res.setX((startX+75)+"px");
        res.setY("150px");
        EndPoint endPointRes = createRectangleEndPoint(EndPointAnchor.TOP, false);
        endPoint.setSource(false);
        endPoint.setTarget(true);
        EndPoint endPointRes2 = createDotEndPoint(EndPointAnchor.BOTTOM, false);
        endPoint.setSource(true);
        endPoint.setTarget(false);
        res.addEndPoint(endPointRes);
        res.addEndPoint(endPointRes2);
        this.model.addElement(res);
        
        Connection conn3 = new Connection(res.getEndPoints().get(0), xor.getEndPoints().get(1));
        this.model.connect(conn3);
        
        Connection conn4 = new Connection(res.getEndPoints().get(1), ele2.getEndPoints().get(1));
        this.model.connect(conn4);
        
    }
    
    /*
    * Usado para que al redibujar
    * padamos tener conocimiento de los taps seleccionados
    * y conectarlos nuevamente
    */
    private void addPreviousConnections() {
        for(int i : this.connections) {
            Element a = this.model.getElements().get(i);
            Element b = this.model.getElements().get(this.model.getElements().size()-1);
            Connection conn = new Connection(a.getEndPoints().get(0), b.getEndPoints().get(0));
            this.model.connect(conn);  
        }
    }
    
    /**
     * @return the binarySeed
     */
    public String getBinarySeed() {
        return binarySeed;
    }

    /**
     * @param binarySeed the binarySeed to set
     */
    public void setBinarySeed(String binarySeed) {
        this.initModel();
        long x = Long.parseLong(binarySeed, 2);
        if ( x > Integer.MAX_VALUE ) {
            this.binarySeed = "1111111111111111111111111111111";
        } else {
            this.binarySeed = binarySeed;
        }     
    }

    /**
     * @return the seed
     */
    public long getSeed() {
        return seed;
    }

    /**
     * @param seed the seed to set
     */
    public void setSeed(long seed) {
        this.initModel();
        if ( seed > Integer.MAX_VALUE ) {
            this.seed = Integer.MAX_VALUE;
        } else {
            this.seed = seed;
        }
    }

    /**
     * @return the lfsr
     */
    public LFSR getLfsr() {
        return lfsr;
    }

    /**
     * @return the rowsPerTemplate
     */
    public String getRowsPerTemplate() {
        return rowsPerTemplate;
    }

    /**
     * @return the model
     */
    public DefaultDiagramModel getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(DefaultDiagramModel model) {
        this.model = model;
    }
    
    /**
    * Crea un nuevo nodo en forma de circulo
    */
    private EndPoint createDotEndPoint(EndPointAnchor anchor, boolean source) {
        DotEndPoint endPoint = new DotEndPoint(anchor);
        endPoint.setMaxConnections(1);
        endPoint.setRadius(5);
        endPoint.setSource(source);
        endPoint.setStyle("{fillStyle:'#9fda58'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }

    /**
    * Crea un nuevo nodo en forma de rectangulo
    */
    private EndPoint createRectangleEndPoint(EndPointAnchor anchor, boolean source) {
        RectangleEndPoint endPoint = new RectangleEndPoint(anchor);
        endPoint.setMaxConnections(1);
        endPoint.setHeight(10);
        endPoint.setWidth(10);
        endPoint.setSource(source);
        endPoint.setTarget(true);
        endPoint.setStyle("{fillStyle:'#9fda58'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }
    
    /**
    * Crea un nuevo nodo en forma de rectangulo, usado para que 
    * cree nodos que solo reciban y no puedan sacar conexiones
    */
    private EndPoint createRectangleEndPoint2(EndPointAnchor anchor, boolean source) {
        RectangleEndPoint endPoint = new RectangleEndPoint(anchor);
        endPoint.setMaxConnections(0);
        endPoint.setHeight(1);
        endPoint.setWidth(1);
        endPoint.setSource(source);
        endPoint.setTarget(true);
        endPoint.setStyle("{fillStyle:'#9fda58'}");
        endPoint.setHoverStyle("{fillStyle:'#5C738B'}");
        return endPoint;
    }
    
    
    /**
    * Se manda a llamar cuando conectamos un tap
    */
    public void onConnect(ConnectEvent event) {
        
        int len = this.binarySeed.length() - 1;
        int position = Integer.parseInt(event.getSourceElement().getId());
        this.connections.add(position);
        this.lfsr.addTap(len-position);
        System.out.println("added position: "+(len-position));
        
    }
    
    /**
    * Se manda a llamar cuando desconectamos un tap
    */    
    public void onDisconnect(DisconnectEvent event) {
        int position = Integer.parseInt(event.getSourceElement().getId());
        this.connections.remove(new Integer(position));
    }

    /**
     * @return the endProcess
     */
    public boolean isEndProcess() {
        return endProcess;
    }

    /**
     * @param endProcess the endProcess to set
     */
    public void setEndProcess(boolean endProcess) {
        this.endProcess = endProcess;
    }
    
    /**
    * Durante el despliegue de resultados
    * se activa cuando se da click en el botón anterior
    * y dibuja el grafo con el corrimiento anterior al actual
    */
    public void anterior() {
        this.stepIndex--;
        if ( this.stepIndex < 0 ) {
            this.stepIndex = 0;
        }
        this.initModel2();
    }
    
    /**
    * Durante el despliegue de resultados
    * se activa cuando se da click en el botón anterior
    * y dibuja el grafo con el corrimiento siguiente al actual
    */
    public void siguiente() {
        this.stepIndex++;
        if ( this.stepIndex >= this.getSteps().size() ) {
            this.stepIndex = this.getSteps().size()-1;
        }
        this.initModel2();
    }
    
    /**
    * Vuelve a resetear todos los parámetros
    * para la simulación
    */
    public void reset() {
        this.lfsr.resetTaps();
        this.steps = new ArrayList<Step>();
        this.tapsAsString();
        this.endProcess = false;
        this.connections = new ArrayList<Integer>();
        this.stepIndex = -1;
        this.steps = new ArrayList<Step>();
        this.rowsPerTemplate = "10, 50, 100";
        this.initModel();
        this.tapsAsString();
    }
    
    /**
    * Método auxiliar de debug
    */
    public String tapsAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for ( int x : this.lfsr.getTaps() ) {
            sb.append(x + ", ");
        }
        sb.append(" ]");
        return sb.toString();
    }
   
    
}
