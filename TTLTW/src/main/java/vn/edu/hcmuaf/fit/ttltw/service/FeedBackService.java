package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.FeedbackDao;
import vn.edu.hcmuaf.fit.ttltw.model.Feedback;

import java.util.List;

public class FeedBackService {
    private FeedbackDao feedbackDao;
    public FeedBackService() {
        this.feedbackDao = new FeedbackDao();
    }
    public List<Feedback> getFeedbacksByProductId(int productId) {
        return feedbackDao.getFeedbacksByProductId(productId);
    }
    public int countByProductId(int productId) {
        return feedbackDao.countByProductId(productId);
    }
    public boolean insertFeedback(Feedback feedback) {
        return feedbackDao.insertFeedback(feedback);
    }
}
