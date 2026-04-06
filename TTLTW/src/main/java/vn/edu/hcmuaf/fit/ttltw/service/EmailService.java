package vn.edu.hcmuaf.fit.ttltw.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailService {

    private static final String host;
    private static final int port;
    private static final String username;
    private static final String password;
    private static final String fromName;

    static {
        try {
            Properties props = new Properties();
            try (InputStream input = EmailService.class
                    .getClassLoader()
                    .getResourceAsStream("email.properties")) {
                if (input == null) throw new RuntimeException("Không tìm thấy email.properties");
                props.load(input);
            }
            host     = props.getProperty("mail.host",      "smtp.gmail.com");
            port     = Integer.parseInt(props.getProperty("mail.port", "587"));
            username = props.getProperty("mail.username");
            password = props.getProperty("mail.password");
            fromName = props.getProperty("mail.from.name", "TTLTW Store");
        } catch (Exception e) {
            throw new RuntimeException("EmailService init error", e);
        }
    }

    public static void sendAccountLocked(String toEmail, String name) throws MessagingException, UnsupportedEncodingException {
        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.auth",            "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");
        smtpProps.put("mail.smtp.host",            host);
        smtpProps.put("mail.smtp.port",            String.valueOf(port));

        Session session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username, fromName, "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Thông báo khóa tài khoản - " + fromName);
        message.setContent(buildAccountLockedTemplate(name), "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private static String buildAccountLockedTemplate(String name) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <style>
                    body { margin: 0; padding: 0; background: #EFEFEF; font-family: Arial, sans-serif; }
                    .wrapper { max-width: 560px; margin: 40px auto; background: #fff;
                               border-radius: 12px; overflow: hidden;
                               box-shadow: rgba(100, 100, 111, 0.2) 0px 7px 29px 0px; }
                    .header { background: rgba(180, 30, 30, 0.92);
                              padding: 32px; text-align: center; }
                    .header h1 { margin: 0; color: #fff; font-size: 26px; letter-spacing: 1px; }
                    .body { padding: 40px 36px; text-align: center; }
                    .body p { color: #444; font-size: 15px; line-height: 1.8; margin: 0 0 12px; }
                    .icon { font-size: 56px; margin-bottom: 16px; }
                    .warning-box { display: inline-block; margin: 20px 0;
                                   background: #fff5f5; border: 2px solid #f5a0a0;
                                   border-radius: 10px; padding: 18px 32px;
                                   color: #c0392b; font-size: 14px; line-height: 1.7; }
                    .footer { background: #EFEFEF; border-top: 1px solid #eee;
                              padding: 18px; text-align: center;
                              color: #777777; font-size: 12px; }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="header"><h1>TTLTW Store</h1></div>
                    <div class="body">
                      <div class="icon">&#128274;</div>
                      <p>Xin chào <strong>""" + name + """
                </strong>,</p>
                      <p>Hệ thống phát hiện tài khoản của bạn đã <strong>đăng nhập sai quá 5 lần</strong>
                         liên tiếp trong vòng 15 phút.</p>
                      <p>Để bảo vệ tài khoản, chúng tôi đã <strong>tạm khóa</strong> tài khoản này.</p>
                      <div class="warning-box">
                        &#9888; Vui lòng liên hệ với bộ phận vận hành để được mở khóa.<br>
                        Nếu không có phản hồi trong thời gian sớm,
                        tài khoản này có thể bị <strong>xóa vĩnh viễn</strong>.
                      </div>
                      <p style="color:#777; font-size:13px;">
                        Nếu chính bạn thực hiện các lần đăng nhập này,<br>
                        hãy liên hệ ngay để chúng tôi hỗ trợ khôi phục tài khoản.
                      </p>
                    </div>
                    <div class="footer">
                      © 2025 TTLTW Store &nbsp;·&nbsp;
                      Email này được gửi tự động, vui lòng không reply.
                    </div>
                  </div>
                </body>
                </html>
                """;
    }

    public static void sendOtp(String toEmail, String otp) throws MessagingException, UnsupportedEncodingException {
        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.auth",            "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");
        smtpProps.put("mail.smtp.host",            host);
        smtpProps.put("mail.smtp.port",            String.valueOf(port));

        Session session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username, fromName, "UTF-8"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Mã xác thực đăng ký tài khoản - " + fromName);
        message.setContent(buildOtpTemplate(otp), "text/html; charset=UTF-8");

        Transport.send(message);
    }

    private static String buildOtpTemplate(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8">
                  <style>
                    body { margin: 0; padding: 0; background: #EFEFEF; font-family: Arial, sans-serif; }
                    .wrapper { max-width: 560px; margin: 40px auto; background: #fff;
                               border-radius: 12px; overflow: hidden;
                               box-shadow: rgba(100, 100, 111, 0.2) 0px 7px 29px 0px; }
                    .header { background: rgba(22, 77, 87, 0.95);
                              padding: 32px; text-align: center; }
                    .header h1 { margin: 0; color: #fff; font-size: 26px; letter-spacing: 1px; }
                    .body { padding: 40px 36px; text-align: center; }
                    .body p { color: #555; font-size: 15px; line-height: 1.7; margin: 0 0 12px; }
                    .otp-box { display: inline-block; margin: 24px 0;
                               background: #f0f8fa; border: 2px dashed #4AA3B3;
                               border-radius: 10px; padding: 18px 44px; }
                    .otp-code { font-size: 46px; font-weight: 800; color: rgba(6, 84, 122, 0.87);
                                letter-spacing: 12px; }
                    .note { color: #777777; font-size: 13px; margin-top: 4px; }
                    .footer { background: #EFEFEF; border-top: 1px solid #eee;
                              padding: 18px; text-align: center;
                              color: #777777; font-size: 12px; }
                  </style>
                </head>
                <body>
                  <div class="wrapper">
                    <div class="header"><h1>TTLTW Store</h1></div>
                    <div class="body">
                      <p>Xin chào,</p>
                      <p>Bạn vừa yêu cầu đăng ký tài khoản tại <strong>TTLTW Store</strong>.<br>
                         Vui lòng nhập mã OTP bên dưới để hoàn tất:</p>
                      <div class="otp-box">
                        <div class="otp-code">""" + otp + """
                        </div>
                      </div>
                      <p class="note">Mã có hiệu lực trong <strong>5 phút</strong>.<br>
                         Không chia sẻ mã này với bất kỳ ai.</p>
                    </div>
                    <div class="footer">
                      © 2025 TTLTW Store &nbsp;·&nbsp;
                      Nếu bạn không yêu cầu điều này, hãy bỏ qua email này.
                    </div>
                  </div>
                </body>
                </html>
                """;
    }
}
