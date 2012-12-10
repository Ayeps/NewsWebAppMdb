package jsf;

import domain.NewsEntity;
import ejb.NewsEntityFacade;
import ejb.SessionManagerBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;

/**
 * This is a JSF/CDI-managed bean that serves as a MVC component providing
 * session storage for new items (model), vectoring to the appropriate view,
 * and control of data processing (controller).
 * <P>
 * It also serves as a JMS producer component (see postNewMessag()), packaging
 * the data from the view into a JMS message and sending it to the JMS queue.
 * Note how this decouples the data from the mechanism that processes the
 * data -- this is totally hidden from this class. Of course, in this sample
 * project we are saving that data to a database, but there's no indication
 * of that here. Also, note that this operation is asynchronous, meaning that
 * when the data is sent to the queue we don't have to wait for a response
 * (fire and forget). We are free to do something else, in this case redirect 
 * to a different view.
 * 
 * @author Instlogin
 */
@Named(value = "listNewsBean")
@SessionScoped
public class ListNewsBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<NewsEntity> newsItems = new ArrayList<NewsEntity>();
    private NewsEntity newsItem;
    private String title;
    private String body;
    
    @Inject
    private SessionManagerBean sessionManagerBean;
    @Inject
    private NewsEntityFacade newsEntityFacade;
    @Resource(mappedName = "jms/NewsMessageFactory")
    private ConnectionFactory connectionFactory;
    @Resource(mappedName = "jms/NewsMessage")
    private Queue queue;

    public ListNewsBean() {
    }

    /**
     * Update the model after the bean is constructed, and then again
     * on demand.
     */
    @PostConstruct
    public void updateMessageList() {
        newsItems = newsEntityFacade.findAll();
    }

    /**
     * Control access to session info.
     * @return the number of active sesssions.
     */
    public int getActiveSessionsCount() {
        return sessionManagerBean.getActiveSessionsCount();
    }

    /**
     * Produce a new JMS message from posted data and send it to the JMS queue.
     * @return the destination view page.
     */
    public String postNewMessage() {
        try {
            Connection connection = connectionFactory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(queue);

            ObjectMessage message = session.createObjectMessage();
            // here we create NewsEntity, that will be sent in JMS message
            newsItem = new NewsEntity();
            newsItem.setTitle(title);
            newsItem.setBody(body);

            message.setObject(newsItem);
            messageProducer.send(message);
            messageProducer.close();
            connection.close();
            
            updateMessageList();

        } catch (JMSException ex) {
            ex.printStackTrace();
        }
        
        return "index";
    }

    public List<NewsEntity> getNewsItems() {
        return newsItems;
    }

    public void setNewsItems(List<NewsEntity> newsItems) {
        this.newsItems = newsItems;
    }

    public NewsEntity getNewsItem() {
        return newsItem;
    }

    public void setNewsItem(NewsEntity newsItem) {
        this.newsItem = newsItem;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    
    
}
