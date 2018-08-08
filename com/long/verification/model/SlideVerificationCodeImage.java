package verification.model;

import java.awt.image.BufferedImage;

public class SlideVerificationCodeImage {
    // 原图，被抠图位置颜色加深
    private BufferedImage bigImage;
    // 抠图
    private BufferedImage smallImage;
    // 抠图偏移量
    private int offset;

    public SlideVerificationCodeImage(BufferedImage bigImage, BufferedImage smallImage, int offset) {
        this.bigImage = bigImage;
        this.smallImage = smallImage;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "SlideVerificationCodeImage{" +
                "bigImage=" + bigImage +
                ", smallImage=" + smallImage +
                ", offset=" + offset +
                '}';
    }

    public BufferedImage getBigImage() {
        return bigImage;
    }

    public SlideVerificationCodeImage setBigImage(BufferedImage bigImage) {
        this.bigImage = bigImage;
        return this;
    }

    public BufferedImage getSmallImage() {
        return smallImage;
    }

    public SlideVerificationCodeImage setSmallImage(BufferedImage smallImage) {
        this.smallImage = smallImage;
        return this;
    }

    public int getOffset() {
        return offset;
    }

    public SlideVerificationCodeImage setOffset(int offset) {
        this.offset = offset;
        return this;
    }
}
