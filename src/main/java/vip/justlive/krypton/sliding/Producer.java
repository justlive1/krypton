package vip.justlive.krypton.sliding;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import javax.imageio.ImageIO;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 验证码生成器
 *
 * @author wubo
 */
public class Producer {

  private static final int DEFAULT_WIDTH = 320;
  private static final int DEFAULT_HEIGHT = DEFAULT_WIDTH / 2;
  private static final int DEFAULT_WIDTH_OFFSET = DEFAULT_WIDTH / 4;
  private static final int DEFAULT_DIAMETER = DEFAULT_WIDTH / 8;
  private static final String TEMPLATE = "data:image/%s;base64,%s";

  private final Random random = new Random();
  /**
   * 图片路径
   */
  private final String path;
  private final int width;
  private final int height;
  private final int widthOffset;
  private final int diameter;

  public Producer(String path) {
    this(path, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_WIDTH_OFFSET, DEFAULT_DIAMETER);
  }

  public Producer(String path, int width, int height, int widthOffset, int diameter) {
    File dir = new File(path);
    if (!dir.exists() || !dir.isDirectory()) {
      throw new IllegalArgumentException("图片目录不正确 " + path);
    }
    this.path = path;
    this.width = width;
    this.height = height;
    this.widthOffset = widthOffset;
    this.diameter = diameter;
  }

  /**
   * 生成验证码
   *
   * @return captcha
   */
  public Captcha create() {
    File dir = new File(path);
    File[] files = dir.listFiles();
    if (files == null || files.length == 0) {
      throw Exceptions.fail("目录下不存在图片");
    }
    File file = files[random.nextInt(files.length)];

    int x = random.nextInt(width - diameter * 2 - widthOffset) + widthOffset;
    int y = random.nextInt(height - diameter * 2) + diameter;
    String formatName = file.getName().substring(file.getName().lastIndexOf(Constants.DOT) + 1);
    try {
      return new Captcha().setFormatName(formatName).setOffset(x)
          .setBackground(toBase64(background(file, x, y), formatName))
          .setJigsaw(toBase64(jigsaw(file, x, y), formatName))
          .setToken(UUID.randomUUID().toString());
    } catch (Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  public boolean validate(Captcha captcha, int offset, int time) {
    boolean b = Math.abs(captcha.getOffset() - offset) < 10;
    if (b) {
      captcha.setValidate(random.nextInt(10000));
    }
    return b;
  }

  private String toBase64(BufferedImage image, String formatName) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, formatName, out);
    return String.format(TEMPLATE, formatName,
        Base64.getEncoder().encodeToString(out.toByteArray()));
  }

  private BufferedImage background(File file, int x, int y) throws Exception {
    BufferedImage bi = ImageIO.read(file);
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.drawImage(bi.getScaledInstance(width, height, 1), null, null);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
    g.setColor(Color.darkGray);
    g.fillPolygon(create(x, y));
    g.dispose();
    return image;
  }

  private BufferedImage jigsaw(File file, int x, int y) throws Exception {
    BufferedImage bi = ImageIO.read(file);
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.drawImage(bi.getScaledInstance(width, height, 1), null, null);
    g.dispose();
    Polygon polygon = create(0, y);
    bi = image.getSubimage(x, 0, diameter / 4 * 5, height);
    image = new BufferedImage(diameter / 4 * 5, height, BufferedImage.TYPE_INT_ARGB);
    for (int i = 0; i < bi.getWidth(); i++) {
      for (int j = 0; j < height; j++) {
        if (polygon.contains(i, j)) {
          image.setRGB(i, j, bi.getRGB(i, j));
        }
      }
    }
    return image;
  }

  private Polygon create(int x, int y) {
    Polygon polygon = new Polygon();
    polygon.addPoint(x, y);
    // 上半圆
    int start = x + diameter / 4;
    int end = x + diameter / 4 * 3;
    int r = diameter / 4;
    for (int i = start; i <= end; i++) {
      int dis = Math.abs(x + diameter / 2 - i);
      polygon.addPoint(i, y - (int) Math.sqrt(r * r - dis * dis));
    }
    polygon.addPoint(x + diameter, y);
    // 右半圆
    start = y + diameter / 4;
    end = y + diameter / 4 * 3;
    for (int i = start; i <= end; i++) {
      int dis = Math.abs(y + diameter / 2 - i);
      polygon.addPoint(x + diameter + (int) Math.sqrt(r * r - dis * dis), i);
    }
    polygon.addPoint(x + diameter, y + diameter);
    polygon.addPoint(x, y + diameter);
    // 左半圆
    start = y + diameter / 4 * 3;
    end = y + diameter / 4;
    for (int i = start; i >= end; i--) {
      int dis = Math.abs(y + diameter / 2 - i);
      polygon.addPoint(x + (int) Math.sqrt(r * r - dis * dis), i);
    }
    return polygon;
  }

}
