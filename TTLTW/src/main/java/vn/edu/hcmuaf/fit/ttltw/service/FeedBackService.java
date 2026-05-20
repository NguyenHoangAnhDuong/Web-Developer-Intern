package vn.edu.hcmuaf.fit.ttltw.service;

import vn.edu.hcmuaf.fit.ttltw.dao.FeedbackDao;
import vn.edu.hcmuaf.fit.ttltw.model.Feedback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public List<Feedback> getFeedbacks(int productId, Integer filterStar) {
        List<Feedback> all = feedbackDao.getFeedbacksByProductId(productId);
        if (filterStar == null) return all;
        return all.stream()
                .filter(fb -> fb.getRating() == filterStar)
                .collect(Collectors.toList());
    }
    public double getAverageRating(int productId) {
        List<Feedback> all = feedbackDao.getFeedbacksByProductId(productId);
        if (all.isEmpty()) return 0;
        int sum = all.stream().mapToInt(Feedback::getRating).sum();
        return Math.round((double) sum / all.size() * 10.0) / 10.0;
    }
    public Map<String, Integer> getStarCounts(int productId) {
        List<Feedback> all = feedbackDao.getFeedbacksByProductId(productId);
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            final int star = i;
            counts.put(String.valueOf(star), // key là String
                    (int) all.stream()
                            .filter(fb -> fb.getRating() == star)
                            .count());
        }
        return counts;
    }
    public List<Feedback> searchFeedbacks(String keyword, Integer star, String status) {
        List<Feedback> all = feedbackDao.getAllFeedbacks();
        return all.stream()
                .filter(f -> {
                    if (keyword == null || keyword.isEmpty()) return true;
                    String kw = keyword.toLowerCase();
                    return (f.getComment()     != null && f.getComment().toLowerCase().contains(kw))
                            || (f.getUsername()    != null && f.getUsername().toLowerCase().contains(kw))
                            || (f.getProductName() != null && f.getProductName().toLowerCase().contains(kw));
                })
                .filter(f -> star == null || f.getRating() == star)
                .filter(f -> {
                    if (status == null || status.isEmpty()) return true;
                    return switch (status) {
                        case "show"    -> f.getStatus() == 1;
                        case "hide"    -> f.getStatus() == 0;
                        case "pending" -> f.getStatus() == 2;
                        default        -> true;
                    };
                })
                .collect(Collectors.toList());
    }
    public void updateStatus(int id, int status) {
        feedbackDao.updateStatus(id, status);
    }
    public void deleteFeedback(int id) {
        feedbackDao.deleteFeedback(id);
    }
}
