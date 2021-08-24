package com.clickntap.api.to;

import java.util.List;

public interface ImageProducer {
  public ImageSet imageSetFromPdf(String pdfUrl, List<Integer> sizes, boolean jpg) throws Exception;

  public ImageSet svgSetFromPdf(String pdfUrl) throws Exception;
}
