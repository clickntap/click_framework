package com.clickntap.tool.pdf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clickntap.tool.f.FContext;

public class PDFContext extends FContext {

  private Number numberOfPages;
  private Number pageNumber;

  public PDFContext() {
    super(null, null);
  }

  public PDFContext(HttpServletRequest request, HttpServletResponse response) {
    super(request, response);
    this.pageNumber = 1;
    this.numberOfPages = 1;
  }

  public Number getNumberOfPages() {
    return numberOfPages;
  }

  public void setNumberOfPages(Number numberOfPages) {
    this.numberOfPages = numberOfPages;
  }

  public Number getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

}
