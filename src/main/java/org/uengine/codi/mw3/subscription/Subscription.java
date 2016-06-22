package org.uengine.codi.mw3.subscription;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.metaworks.ContextAware;
import org.metaworks.MetaworksContext;
import org.metaworks.annotation.AutowiredFromClient;
import org.metaworks.annotation.Hidden;
import org.metaworks.annotation.ServiceMethod;
import org.metaworks.component.SelectBox;
import org.uengine.codi.mw3.billing.catalog.api.BillingPeriod;
import org.uengine.codi.mw3.billing.catalog.api.PriceListSet;
import org.uengine.codi.mw3.billing.catalog.api.ProductCategory;
import org.uengine.codi.mw3.billing.model.Account;
import org.uengine.codi.mw3.billing.model.Subscriptions;
import org.uengine.codi.mw3.model.Company;
import org.uengine.codi.mw3.model.Session;
import org.uengine.codi.util.BillingHttpClient;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by jjy on 2016. 6. 15..
 */
public class Subscription implements ContextAware{

    MetaworksContext metaworksContext;
        @Override
        public MetaworksContext getMetaworksContext() { return metaworksContext; }

        @Override
        public void setMetaworksContext(MetaworksContext metaworksContext) { this.metaworksContext = metaworksContext; }

    public Subscription(){
        setMetaworksContext(new MetaworksContext());
        getMetaworksContext().setWhen(MetaworksContext.WHEN_EDIT);
    }

    @AutowiredFromClient
    public Session session;


    @Hidden
    public String subscriptInfo;
        public String getsubscriptInfo() { return subscriptInfo; }
        public void setsubscriptInfo(String subscriptInfo) { this.subscriptInfo = subscriptInfo; }

    @ServiceMethod
    public void unsubscribe(){
        BillingHttpClient billingHttpClient = new BillingHttpClient();
        Subscriptions result = billingHttpClient.cancelSubscription(session.getCompany().getKillbillSubscription(), session.getCompany().getComName());
        System.out.println(result);
    }

    @ServiceMethod(callByContent=true)
    public void changePlan(){

        String selectedSubscription = this.getPlanSelected().getSelected();

        BillingHttpClient billingHttpClient = new BillingHttpClient();

        Subscriptions subscription = new Subscriptions();
        subscription.setAccountId(UUID.fromString(session.getCompany().getKillbillAccount()));
        subscription.setSubscriptionId(UUID.fromString(session.getCompany().getKillbillSubscription()));
        subscription.setProductName(selectedSubscription);
        subscription.setProductCategory(ProductCategory.BASE);
        subscription.setBillingPeriod(BillingPeriod.MONTHLY);
        subscription.setPriceList(PriceListSet.DEFAULT_PRICELIST_NAME);
        Subscriptions result = billingHttpClient.updateSubscription(subscription, session.getCompany().getComName());
        System.out.println(result);

    }

    @ServiceMethod(callByContent=true)
    public void subscribe(){

        String selectedSubscription = this.getPlanSelected().getSelected();

        String billingSubscription = session.getCompany().getKillbillSubscription();

        if(billingSubscription == null) {

            BillingHttpClient billingHttpClient = new BillingHttpClient();

            Subscriptions subscription = new Subscriptions();
            subscription.setAccountId(UUID.fromString(session.getCompany().getKillbillAccount()));
            subscription.setProductName(selectedSubscription);
            subscription.setProductCategory(ProductCategory.BASE);
            subscription.setBillingPeriod(BillingPeriod.MONTHLY);
            subscription.setPriceList(PriceListSet.DEFAULT_PRICELIST_NAME);
            subscription = billingHttpClient.createSubscription(subscription, session.getCompany().getComName());

            session.getCompany().setKillbillSubscription(subscription.getSubscriptionId().toString());

            setMetaworksContext(new MetaworksContext());
            getMetaworksContext().setWhen(MetaworksContext.HOW_EVER);

            Company company = new Company();
            try {
                company.copyFrom(session.getCompany());
                company.setKillbillSubscription(subscription.getSubscriptionId().toString());
                company.syncToDatabaseMe();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //TODO
        }
    }


    SelectBox planSelected;
        public SelectBox getPlanSelected() {
            return planSelected;
        }
        public void setPlanSelected(SelectBox planSelected) {
            this.planSelected = planSelected;
        }

    public void load() {

        //load current subscription for session

        String billingAccount = session.getCompany().getKillbillAccount();
        if(billingAccount == null) {
            BillingHttpClient billingHttpClient = new BillingHttpClient();

            Account account = new Account();
            account.setName(session.getCompany().getComName());
            account.setCurrency("USD");
            account.setTimeZone("UTC");
            account.setCompany(session.getCompany().getComName());
            account.setIsMigrated(true);
            account.setIsNotifiedForInvoices(true);
            account = billingHttpClient.createAccount(account, session.getCompany().getComName());

            session.getCompany().setKillbillAccount(account.getAccountId().toString());

            Company company = new Company();
            try {
                company.copyFrom(session.getCompany());
                company.setKillbillAccount(account.getAccountId().toString());
                company.syncToDatabaseMe();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String subscriptionInfo = "";
            String billingSubscriptionId = session.getCompany().getKillbillSubscription();
            if(billingSubscriptionId != null) {
                BillingHttpClient billingHttpClient = new BillingHttpClient();
                Subscriptions restulSubscriptions = billingHttpClient.getSubscription(billingSubscriptionId);
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    System.out.println(objectMapper.writeValueAsString(restulSubscriptions));
                    this.setsubscriptInfo(objectMapper.writeValueAsString(restulSubscriptions));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            } else {
                //TODO
            }
        }

        setPlanSelected(new SelectBox());

        ArrayList<String> plans = new ArrayList<>();

        plans.add("Free");
        plans.add("Basic");
        plans.add("Enterprise");

        getPlanSelected().add("Free", "Free");
        getPlanSelected().add("Basic", "Basic");
        getPlanSelected().add("Enterprise", "Enterprise");
        getPlanSelected().setSelected("Basic"); //value from killbill
        getPlanSelected().setSelectedValue("Basic"); //value from killbill
    }
}
