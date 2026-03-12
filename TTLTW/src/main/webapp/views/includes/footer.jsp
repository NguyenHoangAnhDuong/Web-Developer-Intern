<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/asset/css/footer.css">
<footer id="footer">
    <div class="container">
        <div id="footer-content" class="footer-content">
            <div id="footer-company" class="footer-section">
                <div id="footer-logo" class="footer-logo">
                    <img src="${pageContext.request.contextPath}/asset/img/logo.png" alt="Logo công ty">
                </div>
                <p id="footer-description" class="company-description">
                    Chúng tôi cam kết mang đến những sản phẩm chất lượng cao và dịch vụ tốt nhất cho khách hàng.
                </p>
                <div id="footer-social" class="social-links">
                    <a href="#" class="social-link" title="Facebook" id="social-facebook">
                        <i class="fab fa-facebook-f"></i>
                    </a>
                    <a href="#" class="social-link" title="Instagram" id="social-instagram">
                        <i class="fab fa-instagram"></i>
                    </a>
                    <a href="#" class="social-link" title="Twitter" id="social-twitter">
                        <i class="fab fa-twitter"></i>
                    </a>
                    <a href="#" class="social-link" title="YouTube" id="social-youtube">
                        <i class="fab fa-youtube"></i>
                    </a>
                </div>
            </div>

            <div id="footer-links" class="footer-section">
                <h3 class="footer-title">Liên kết nhanh</h3>
                <ul id="quick-links" class="footer-links">
                    <li><a href="${pageContext.request.contextPath}/home" id="link-home">Trang chủ</a></li>
                    <li><a href="${pageContext.request.contextPath}/cart" id="link-cart">Giỏ hàng</a></li>
                    <li><a href="${pageContext.request.contextPath}/cart" id="link-checkout">Thanh toán</a></li>
                    <li><a href="${pageContext.request.contextPath}/user/order" id="link-orders">Đơn hàng</a></li>
                    <li><a href="${pageContext.request.contextPath}/user/profile" id="link-user">Thông tin cá nhân</a></li>
                </ul>
            </div>

            <div id="footer-support" class="footer-section">
                <h3 class="footer-title">Hỗ trợ khách hàng</h3>
                <ul id="support-links" class="footer-links">
                    <li><a href="#" id="policy-return">Chính sách đổi trả</a></li>
                    <li><a href="#" id="policy-privacy">Chính sách bảo mật</a></li>
                    <li><a href="#" id="policy-terms">Điều khoản sử dụng</a></li>
                    <li><a href="#" id="policy-guide">Hướng dẫn mua hàng</a></li>
                    <li><a href="#" id="policy-faq">Câu hỏi thường gặp</a></li>
                </ul>
            </div>

            <div id="footer-contact" class="footer-section">
                <h3 class="footer-title">Thông tin liên hệ</h3>
                <div id="contact-info" class="contact-info">
                    <div class="contact-item" id="contact-address">
                        <i class="fas fa-map-marker-alt"></i>
                        <span>123 Đường ABC, Quận XYZ, TP.HCM</span>
                    </div>
                    <div class="contact-item" id="contact-phone">
                        <i class="fas fa-phone"></i>
                        <span>+84 123 456 789</span>
                    </div>
                    <div class="contact-item" id="contact-email">
                        <i class="fas fa-envelope"></i>
                        <span>info@company.com</span>
                    </div>
                </div>
            </div>

        </div>
        <div id="footer-bottom" class="footer-bottom">
            <div id="footer-bottom-content" class="footer-bottom-content">
                <p id="footer-copyright">
                    &copy; 2024 Công ty ABC. Tất cả quyền được bảo lưu.
                </p>
            </div>
        </div>
    </div>
</footer>

