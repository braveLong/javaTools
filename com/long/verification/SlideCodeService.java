package verification;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import verification.model.SlideVerificationCodeImage;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;

@Service
public class SlideCodeService {
    private static final Logger logger = LoggerFactory.getLogger(SlideCodeService.class);
    // 拼图外
    private static final int OUT_OF_PUZZLE = 0;
    // 拼图内
    private static final int IN_PUZZLE = 1;
    // 拼图轮廓
    private static final int EDGE = 2;

    // 圆形轮廓误差修正变量，实验发现:原图越大此修正值应该越大
    private static final int DEFAULT_MODIFIERS = 14;
    // 被抠图的颜色加深量
    private static final int RGB_DEEP_DEGREE = 130;

    private static Color TRANSPARENT = new Color(255, 255, 255, 0);
    private static Color BLACK = new Color(0, 0, 0, 255);
    // 验证码图片源
    private List<URL> originImageList;
    private Random random = new Random(17);

    @PostConstruct
    public void init() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL u1 = classLoader.getResource("image/1.png");
        URL u2 = classLoader.getResource("image/2.png");
        URL u3 = classLoader.getResource("image/3.png");
        URL u4 = classLoader.getResource("image/4.png");
        URL u5 = classLoader.getResource("image/5.png");
        URL u6 = classLoader.getResource("image/6.png");

        Assert.notNull(u1, "1.png load error");
        Assert.notNull(u2, "2.png load error");
        Assert.notNull(u3, "3.png load error");
        Assert.notNull(u4, "4.png load error");
        Assert.notNull(u5, "5.png load error");
        Assert.notNull(u6, "6.png load error");

        originImageList = Lists.newArrayList(u1, u2, u3, u4, u5, u6);
    }

    /**
     * 获取滑动图片
     * 包括被抠取拼图的原图、抠取的拼图、拼图横坐标偏移量
     */
    public SlideVerificationCodeImage getSlideCodeImage() throws IOException {
        BufferedImage originImage = getOriginImage();

        Pair<int[][], Integer> coordinate = getCoordinate(originImage.getWidth(), originImage.getHeight());

        BufferedImage puzzle = cutFromOrigin(originImage, coordinate.getLeft(), coordinate.getRight());

        SlideVerificationCodeImage result = new SlideVerificationCodeImage(originImage, puzzle, coordinate.getRight());
        logger.info("verification.SlideCodeService.getSlideCodeImage end , offset:{}", coordinate.getRight());
        return result;
    }


    private BufferedImage getOriginImage() throws IOException {
        URL url = originImageList.get(random.nextInt(originImageList.size()));
        return ImageIO.read(url);
    }

    /**
     * 在指定的宽高矩阵中生成随机拼图的坐标
     * 拼图为正方形，左侧抠掉半个小圆，上侧增加半个小圆
     * @param width 矩阵宽
     * @param height 矩阵高
     * @return pair.left 拼图块的坐标矩阵
     *         例：
     *          0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
     *          0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
     *          0 0 2 2 2 2 2 2 0 0 0 0 0 2 2 2 2 2 2 0 0 0 0 0 0 0 0
     *          0 0 2 1 1 1 1 2 0 0 0 0 0 2 1 1 1 1 2 0 0 0 0 0 0 0 0
     *          0 0 2 1 1 1 1 2 2 0 0 0 2 2 1 1 1 1 2 0 0 0 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 2 2 2 2 2 1 1 1 1 1 2 0 0 0 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 2 1 1 1 1 1 1 1 2 0 0 0 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 2 2 0 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 2 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 2 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 2 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 2 2 0 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 0 0 0 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 0 0 0 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 0 0 0 0 0 0 0 0
     *          0 0 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 2 0 0 0 0 0 0 0 0
     *          0 0 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 0 0 0 0 0 0 0 0
     *          0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
     *          0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
     *          （打印出来的矩阵图是与结果图关于y=x对称的图，不知为何）
     *          （实际获得的图片是与结果图关于x轴对称的图，不知为何）
     *         pair.right 拼图相对原图的偏移量,用于对拼图平移
     */
    private Pair<int[][], Integer> getCoordinate(int width, int height) {
        int[][] data = new int[width][height];

        int minValue = Math.min(width, height);
        // 拼图中心点=矩形中心加减随机偏移量
        double xO = width/2 + (Math.random() - 0.5)* 0.5 * minValue;
        double yO = height/2 + (Math.random() - 0.5)* 0.5 * minValue;
        // 拼图正方形边长=原图边长的五分之一
        double d = minValue/5;
        // 拼图圆形轮廓的半径
        double r = d/4;
        double po = r * r;

        // 拼图最右上点坐标
        double maxX = xO + d/2;
        double maxY = yO + d/2;
        // 拼图最左下点坐标
        double minX = xO - d/2;
        double minY = yO - d/2;

        // 计算所有坐标是否落在拼图内，在内=IN_PUZZLE，轮廓=EDGE，不在=OUT_OF_PUZZLE
        for (int i = (int)minX; i < maxX; i++) {
            for (int j = (int)minY; j < maxY + r; j++) {
                //上边○
                double d1 = Math.pow(i - xO, 2) + Math.pow(j - maxY, 2);
                //左边○
                double d2 = Math.pow(i - minX, 2) + Math.pow(j - yO, 2);
                if (d1 <= po) {
                    if(d1 >= (po - DEFAULT_MODIFIERS) && j >= maxY) {
                        // 在拼图上圆上半弧上
                        data[i][j] = EDGE;
                    } else {
                        data[i][j] = IN_PUZZLE;
                    }
                } else if (d2 <= po) {
                    if(d2 >= (po - DEFAULT_MODIFIERS) && i >= minX) {
                        // 在拼图左圆右半弧上
                        data[i][j] = EDGE;
                    } else {
                        data[i][j] = OUT_OF_PUZZLE;
                    }
                } else if ( i >= minX && i <= maxX && j >= minY && j <= maxY) {
                    if (Math.abs(i - minX) <= 1 || Math.abs(i - maxX) <= 1 || Math.abs(j - maxY) <= 1 || Math.abs(j - minY) <= 1) {
                        // 在拼图正方形边上
                        data[i][j] = EDGE;
                    } else {
                        data[i][j] = IN_PUZZLE;
                    }
                } else {
                    data[i][j] = OUT_OF_PUZZLE;
                }
            }
        }
        return Pair.of(data, (int)(xO - d/2));
    }

    /**
     * 从原图中抠图，原图对应位置颜色加深
     * @param oriImage 原图
     * @param puzzleCoordinate 拼图矩阵坐标
     * @param offset 偏移量
     * @return 抠图
     */
    private BufferedImage cutFromOrigin(BufferedImage oriImage , int[][] puzzleCoordinate, int offset){
        BufferedImage puzzle = new BufferedImage(oriImage.getWidth(), oriImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < puzzleCoordinate.length; i++) {
            for (int j = 0; j < puzzleCoordinate[i].length; j++) {
                int flag = puzzleCoordinate[i][j];
                int rgb_ori = oriImage.getRGB(i, j);

                if (flag == IN_PUZZLE) {
                    //抠图上复制对应颜色值
                    puzzle.setRGB(i - offset, j, rgb_ori);
                    puzzle.setRGB(i , j, TRANSPARENT.getRGB());
                    //原图对应位置颜色变化
                    int r = (0xff & rgb_ori) - RGB_DEEP_DEGREE ;
                    int g = (0xff & (rgb_ori >> 8)) - RGB_DEEP_DEGREE;
                    int b = (0xff & (rgb_ori >> 16)) - RGB_DEEP_DEGREE;
                    r = ((r < 0) ? 0 : r);
                    g = ((g < 0) ? 0 : g);
                    b = ((b < 0) ? 0 : b);
                    rgb_ori = r + (g << 8) + (b << 16) + (0 << 24);
                    oriImage.setRGB(i, j,rgb_ori);
                } else if (flag == EDGE) {
                    // 拼图轮廓描边
                    puzzle.setRGB(i - offset, j, BLACK.getRGB());
                    puzzle.setRGB(i, j, TRANSPARENT.getRGB());
                } else {
                    // 透明化
                    puzzle.setRGB(i, j, TRANSPARENT.getRGB());
                }
            }
        }
        return puzzle;
    }
}
