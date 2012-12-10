package ejb;

import domain.NewsEntity;
import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * This class is an example of a EJB Message Driven Bean -- the consumer of a
 * Point-to-Point JMS architecture. (See the ListNewsBean for an example of
 * producer code.)
 * <P>
 * Why use an MDB here? There are at least two advantages in this example:
 * <P>
 * 1) As a consumer, this object acts as a listener to a JMS queue. It 
 * will automatically consume a message asynchronously whenever a message
 * arrives at the JMS queue. Being asynchronous, the producer component does
 * not have to wait for a response to be generated. The producer can go do
 * something else (fire and forget).
 * <P>
 * 2) Since the producer
 * talks only to the JMS Queue, the handling of the information is totally
 * decoupled from the production of the information. This means that while we
 * are currently using a database to store the information, the processing of
 * this information could change in the future without breaking the producer
 * code. For example, we could choose to save the info to a file, or do 
 * something radically different with the information.
 * 
 * @author Instlogin
 */
@MessageDriven(mappedName = "jms/NewsMessage", activationConfig = {
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class NewsMessageBean implements MessageListener {

    @Resource
    private MessageDrivenContext mdc;
    @PersistenceContext(unitName = "NewsWebAppMdbPU")
    private EntityManager em;

    public NewsMessageBean() {
    }

    /**
     * Listens for messages arriving in the JMS queue and then acts on the
     * presence by saving the message in a database. This could easily be
     * changed to do something else with the info.
     * 
     * @param message - a JMS representation of the data.
     */
    @Override
    public void onMessage(Message message) {
        ObjectMessage msg = null;
        try {
            if (message instanceof ObjectMessage) {
                msg = (ObjectMessage) message;
                NewsEntity e = (NewsEntity) msg.getObject();
                save(e);
            }
        } catch (JMSException e) {
            e.printStackTrace();
            mdc.setRollbackOnly();
        } catch (Throwable te) {
            te.printStackTrace();
        }
    }

    /**
     * Currently we save the information to a database. But we could easily
     * change this implementation to do something else.
     * @param object - the data to be saved.
     */
    public void save(Object object) {
        em.persist(object);
    }
}
