package org.uengine.social;

import org.metaworks.annotation.Id;
import org.metaworks.dwr.MetaworksRemoteService;
import org.metaworks.widget.Label;
import org.metaworks.widget.ModalWindow;
import org.uengine.kernel.DefaultActivity;
import org.uengine.modeling.ElementView;
import org.uengine.modeling.ElementViewActionDelegate;

/**
 * Created by jjy on 2015. 11. 6..
 */
public class ElementViewActionDelegateForInstanceMonitoring implements ElementViewActionDelegate{

    String instanceId;
    @Id
        public String getInstanceId() {
            return instanceId;
        }
        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }


    @Override
    public void onDoubleClick(ElementView elementView) {

        if(elementView.getElement()!=null && elementView.getElement() instanceof DefaultActivity){
            MetaworksRemoteService.wrapReturn(new ModalWindow(new Label(elementView.getElement().getDescription()), elementView.getElement().getName()));
        }
    }
}
