package com.clickntap.platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class SvgColorsUtils {

  public static void findColors(Element element, List<String> colors) {
    findColor(element, colors, "fill");
    findColor(element, colors, "stroke");
    for (Element child : element.elements()) {
      findColors(child, colors);
    }
  }

  public static void findColor(Element element, List<String> colors, String attribute) {
    String color = element.attributeValue(attribute);
    color = ColorUtil.format(color, ColorUtil.HTML_FORMAT);
    if (color != null) {
      if (!colors.contains(color)) {
        colors.add(color);
      }
    }
  }

  public static List<String> findColors(String logoSvg) throws Exception {
    Element rootElement = (Element) DocumentHelper.parseText(logoSvg).getRootElement();
    List<String> colors = new ArrayList<String>();

    colors.add("#000000");
    colors.add("#FFFFFF");

    findColors(rootElement, colors);
    return colors;
  }

  public static void main(String[] args) throws IOException {
    System.out.println(ColorUtil.format("rgb(242,242,247)", ColorUtil.HTML_FORMAT));
  }

}
