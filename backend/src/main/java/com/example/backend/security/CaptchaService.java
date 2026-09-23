package com.example.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码服务：生成 4 位随机验证码图片（base64 PNG），存 Redis 一次性核销（TTL 5 分钟）。
 * 由配置开关 {@code app.security.captcha-enable} 控制是否启用。
 */
@Component
public class CaptchaService {

    /** 验证码位数 */
    private static final int LENGTH = 4;
    /** 验证码有效时长（秒） */
    private static final long TTL_SECONDS = 5L * 60;
    private static final String PREFIX = "auth:captcha:";
    /** 避免易混淆字符 O/0、I/l/1 */
    private static final String CHARS = "23456789ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${app.security.captcha-enable:false}")
    private boolean captchaEnabled;

    public CaptchaService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /** 验证码是否启用 */
    public boolean isEnabled() {
        return captchaEnabled;
    }

    /** 生成验证码记录，返回 {id, base64Png}；验证码未启用时返回 null */
    public CaptchaAnswer generate() {
        if (!captchaEnabled) {
            return null;
        }
        String code = randomCode(LENGTH);
        String id = Long.toHexString(System.nanoTime());
        try {
            stringRedisTemplate.opsForValue().set(PREFIX + id, code, TTL_SECONDS, TimeUnit.SECONDS);
        } catch (DataAccessException ex) {
            // Redis 不可用时不生成验证码，前端走无验证码登录
            return null;
        }
        return new CaptchaAnswer(id, "data:image/png;base64," + renderBase64(code));
    }

    /** 校验并核销验证码（一次性）。验码不通过或 Redis 不可用时校验失败 */
    public boolean verify(String id, String code) {
        if (!captchaEnabled) {
            return true;
        }
        if (id == null || code == null || id.isBlank() || code.isBlank()) {
            return false;
        }
        try {
            String stored = stringRedisTemplate.opsForValue().get(PREFIX + id);
            if (stored == null) {
                return false;
            }
            stringRedisTemplate.delete(PREFIX + id); // 一次性核销
            return stored.equalsIgnoreCase(code.trim());
        } catch (DataAccessException ex) {
            return false;
        }
    }

    private String randomCode(int len) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /** 绘制验证码 PNG 并转 base64（Java 2D，简单噪点+斜线干扰） */
    private String renderBase64(String code) {
        int w = 120;
        int h = 40;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0x13, 0x41, 0x55));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(0xD1, 0xFF, 0xFF));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        for (int i = 0; i < code.length(); i++) {
            int x = 20 + i * 22;
            int y = 27 + ThreadLocalRandom.current().nextInt(-3, 4);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }
        // 干扰斜线
        g.setColor(new Color(0x2E, 0x5A, 0x6F));
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 5; i++) {
            g.draw(new Line2D.Float(
                    ThreadLocalRandom.current().nextInt(w),
                    ThreadLocalRandom.current().nextInt(h),
                    ThreadLocalRandom.current().nextInt(w),
                    ThreadLocalRandom.current().nextInt(h)));
        }
        g.dispose();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("生成验证码图片失败", e);
        }
    }

    /** 验证码应答记录 */
    public record CaptchaAnswer(String id, String imageBase64) {
    }
}