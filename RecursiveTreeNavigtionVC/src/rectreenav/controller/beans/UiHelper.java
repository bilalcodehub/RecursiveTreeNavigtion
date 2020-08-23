package rectreenav.controller.beans;

import java.math.BigDecimal;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;

import javax.el.ValueExpression;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.ADFContext;
import oracle.adf.view.rich.component.rich.data.RichListView;
import oracle.adf.view.rich.component.rich.layout.RichDeck;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;
import oracle.jbo.Row;

import org.apache.myfaces.trinidad.event.SelectionEvent;

public class UiHelper {
    private RichDeck animationDeck;
    private RichListView empsListView;

    public UiHelper() {
    }

    public void setAnimationDeck(RichDeck animationDeck) {
        this.animationDeck = animationDeck;
    }

    public RichDeck getAnimationDeck() {
        return animationDeck;
    }

    public void onBackNavigationAction(ActionEvent actionEvent) {
        // Add event code here...
        BigDecimal managerId = (BigDecimal) actionEvent.getComponent().getAttributes().get("ManagerId");
        OperationBinding oper = findOperation("GetSubordinatesForSelectedManager");
        oper.getParamsMap().put("ParamManagerId", managerId);
        oper.execute();
        ADFContext.getCurrent().getPageFlowScope().put("transition","slideRight");
        AdfFacesContext.getCurrentInstance().addPartialTarget(empsListView);
        AdfFacesContext.getCurrentInstance().addPartialTarget(animationDeck);
        
    }

    public Object invokeMethodExpression(String expr, Class returnType, Class[] argTypes, Object[] args) {
        
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elctx = fc.getELContext();
        ExpressionFactory elFactory = fc.getApplication().getExpressionFactory();
        MethodExpression methodExpr = elFactory.createMethodExpression(elctx, expr, returnType, argTypes);
        return methodExpr.invoke(elctx, args);
    }
    
    public void setEmpsListView(RichListView empsListView) {
        this.empsListView = empsListView;
    }

    public RichListView getEmpsListView() {
        return empsListView;
    }

    public void onEmployeesRowSelection(SelectionEvent selectionEvent) {
        // Add event code here...
        // 
        invokeMethodExpression("#{bindings.EmployeeVO1.treeModel.makeCurrent}", Object.class, new Class[] { SelectionEvent.class }, new Object[] { selectionEvent });
        
        Row r = findIterator("EmployeeVO1Iterator").getCurrentRow();
        
        if (r.getAttribute("HasChildren").equals("Y")) {
            
            BigDecimal managerId = (BigDecimal) r.getAttribute("EmployeeId");
            
            OperationBinding oper = findOperation("GetSubordinatesForSelectedManager");
            oper.getParamsMap().put("ParamManagerId", managerId);
            oper.execute();
            ADFContext.getCurrent().getPageFlowScope().put("transition","slideLeft");

            AdfFacesContext.getCurrentInstance().addPartialTarget(empsListView);
            AdfFacesContext.getCurrentInstance().addPartialTarget(animationDeck);
        }
    }
    
    public static OperationBinding findOperation(String name) {
        OperationBinding op = 
            getDCBindingContainer().getOperationBinding(name);
        if (op == null) {
            throw new RuntimeException("Operation '" + name + "' not found");
        }
        return op;
    }
    
    public static BindingContainer getBindingContainer() {
        return (BindingContainer) resolveExpression("#{bindings}");
    }

  
    public static DCBindingContainer getDCBindingContainer() {
        return (DCBindingContainer)getBindingContainer();
    }
    
    public static Object resolveExpression(String expression) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application app = facesContext.getApplication();
        ExpressionFactory elFactory = app.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExp = 
            elFactory.createValueExpression(elContext, expression, 
                                            Object.class);
        return valueExp.getValue(elContext);
    }
    
    public static DCIteratorBinding findIterator(String name) {
        DCIteratorBinding iter = 
            getDCBindingContainer().findIteratorBinding(name);
        if (iter == null) {
            throw new RuntimeException("Iterator '" + name + "' not found");
        }
        return iter;
    }
    
}
