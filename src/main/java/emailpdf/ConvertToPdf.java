package emailpdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.mail.MessagingException;

import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

/**
 * Created by Prashant on 12/07/2017.
 */

public class ConvertToPdf {
	
	public static void  query(Date from1,Date to1) throws IOException{  
	//Change Date Format Based on Your Requirement.
	SimpleDateFormat DATE_FORMT = new SimpleDateFormat("dd-MMM-yyyy");
        
	String from = DATE_FORMT.format(from1);
        String to = DATE_FORMT.format(to1);
	System.out.print(from+" "+to);
		
	//Get properties of database from config.properties file. 
	Properties prop = new Properties();
        InputStream input = new FileInputStream("config.properties");
        prop.load(input);
        String host = prop.getProperty("host");
        String port = prop.getProperty("portno");
        String sid = prop.getProperty("sid");
        String username = prop.getProperty("username");
        String password = prop.getProperty("password");
        input.close();
        
	//Format for amount as per company's reuirement.
        DecimalFormat decimalFormat = new DecimalFormat("#.00"); //Precision of 2 after decimal.
	decimalFormat.setGroupingUsed(true);
	decimalFormat.setGroupingSize(3); //Grouping of 3 by comma.
        
	EmailPdf.init();//Initializing email property.
	    
        //Driver for Oracle DataBase.
        try {
   	         Class.forName("oracle.jdbc.driver.OracleDriver");     
             } catch (ClassNotFoundException e) {
   	         e.printStackTrace();
   	     }
        
        try {
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@"+host+":"+port+":"+sid,username,password);
        //Query for Merchant Details.
        PreparedStatement s = conn.prepareStatement("Write Query of Merchant Details");
	ResultSet rs = s.executeQuery();
	//Query for every merchant.
	while(rs.next()) {
		String merchant_id = rs.getString("MERCHANT_ID");
		String merchant_accountno = rs.getString("MERCHANT_ACCOUNT_NO");
		String branch = merchant_accountno.substring(0,3);
		String max_net_amountdb = "Provide Query to select merchant's max net amount from provided date 'from' & 'to' with respect to merchant_id";
	        PreparedStatement s1 = conn.prepareStatement(max_net_amountdb);
		ResultSet rs1 = s1.executeQuery();
		String max_net_amount = null;
		if(rs1.next())
			max_net_amount = String.valueOf(decimalFormat.format(rs1.getDouble("MAX(NET_AMOUNT)")));
		//String conversion converts null value to 0. So, we provide 'rs.wasNull' to prevent from parsing 0 value.  
		if(rs1.wasNull())
			max_net_amount = null;
		rs1.close(); //close to prevent cursor from dangling.
		s1.close();
		  
		if(max_net_amount!=null) { // if the merchant doesnot have transaction within provided time interval then skip that client. 
			String filename = "temp/sample.pdf";	
			String image ="logo\\sbl65.gif";
			File file = new File(filename);
	        	file.getParentFile().mkdirs(); //make directory if not exist.
		
	       		//Set Pdf Document.
           		PdfDocument pdfDoc = new PdfDocument(new PdfWriter(filename));
           		Document doc = new Document(pdfDoc, PageSize.LETTER);
           		doc.setMargins(30, 12, 30, 12); //top,right,bottom,left margin of document.
        
           		//Set Current Date.
           		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy");
           		LocalDate localDate = LocalDate.now();
           		String current_date = dtf.format(localDate); 
        
           		Image sbl = new Image(ImageDataFactory.create(image));
        
           		//Header Table for Logo,heading and current date.
           		Table table1 = new Table(new float[] {1,1,1});
           		table1.setFixedLayout();
           		table1.setWidthPercent(100);
           		table1.setMarginBottom(15); //space between logo and table 15px.
        
           		Cell cell = new Cell().add(sbl); //Cell for Image.
           		cell.setBorder(Border.NO_BORDER);
        
           		//Cell for Advice of Credit.
           		Cell cell1 = new Cell().add(new Paragraph("Advice of Credit").setBold().setFontSize(14));
           		cell1.setTextAlignment(TextAlignment.CENTER);
           		cell1.setVerticalAlignment(VerticalAlignment.MIDDLE);
           		cell1.setBorder(Border.NO_BORDER);
        
           		//Cell for current date.
           		Cell cell2 = new Cell().add(new Paragraph("Print Date: "+current_date).setFontSize(9));
           		cell2.setBorder(Border.NO_BORDER);
           		cell2.setTextAlignment(TextAlignment.CENTER);
        
           		//Add cell in respective table and then, document.
           		table1.addCell(cell);
           		table1.addCell(cell1);
           		table1.addCell(cell2);
           		doc.add(table1);
           		//End of header logo table 
        
           		//Start of Table 
           		Table table2 = new Table(new float[] {1,5,3,5,3,3,4,4,3,3}); //mention table with fixed width ratio.
           		table2.setFixedLayout(); 
           		table2.setWidthPercent(100);
        	
           		//getCell method is a user-define method to set common properties.
           		table2.addCell(getCell(1,10,"Merchant Details",FontConstants.HELVETICA_BOLD,10,5,Color.RED,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,4,"Merchant Name:",FontConstants.HELVETICA,10,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,6,rs.getString("MERCHANT_NAME"),FontConstants.HELVETICA_BOLD,9,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,4,"Merchant Address:",FontConstants.HELVETICA,10,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,6,rs.getString("LOCATION"),FontConstants.HELVETICA,9,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,4,"Phone Number:",FontConstants.HELVETICA,10,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,6,rs.getString("landlineno1"),FontConstants.HELVETICA,9,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,4,"Merchant E-mail ID:",FontConstants.HELVETICA,10,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,6,rs.getString("EMAIL_ID"),FontConstants.HELVETICA,9,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,4,"Total Credit Amount:",FontConstants.HELVETICA,10,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,2,max_net_amount,FontConstants.HELVETICA,9,5,Color.BLACK,TextAlignment.RIGHT,15));
           		table2.addCell(getCell(1,2,"Currency",FontConstants.HELVETICA,9,5,Color.BLACK,TextAlignment.RIGHT,15));
           		table2.addCell(getCell(1,2,"NPR",FontConstants.HELVETICA,9,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,8,"Branch",FontConstants.HELVETICA,9,5,Color.BLACK,TextAlignment.RIGHT,15));
           		table2.addCell(getCell(1,2,branch,FontConstants.HELVETICA,9,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,4,"Account Number:",FontConstants.HELVETICA,10,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(getCell(1,6,merchant_accountno,FontConstants.HELVETICA,9,5,Color.BLACK,TextAlignment.LEFT,15));
           		table2.addCell(new Cell(1,10).setHeight(12));
        
           		Cell cell3 = new Cell(1,11).add(new Paragraph("Transaction Details").setBold().setFontSize(10).setPaddingLeft(5).setFontColor(Color.RED));
           		Paragraph p = new Paragraph().setFixedLeading(24).setPaddingLeft(5);
           		p.add(new Text("We have credited your account as per the Merchant settlement report dated ").setFontSize(10));
        
           		p.add(new Text(from1+"  to  "+to1).setFontSize(9)); //Add the dynamic date of transaction here.
           		cell3.add(p);
           		cell3.setHeight(40);
           		cell3.setVerticalAlignment(VerticalAlignment.MIDDLE);
           		cell3.setTextAlignment(TextAlignment.LEFT);
           		table2.addCell(cell3);
        
           		//Customize Color Code.(for table header)
           		Color mycolor = new DeviceRgb(224 ,234 ,243);
        
           		//Table heading
           		table2.addCell(getCellHead("No.",10,FontConstants.HELVETICA_BOLD,TextAlignment.CENTER,mycolor));
           		table2.addCell(getCellHead("Merchant Name",10,FontConstants.HELVETICA_BOLD,TextAlignment.CENTER,mycolor));
           		table2.addCell(getCellHead("Card Type",10,FontConstants.HELVETICA_BOLD,TextAlignment.CENTER,mycolor));
           		table2.addCell(getCellHead("Card Number",10,FontConstants.HELVETICA_BOLD,TextAlignment.CENTER,mycolor));
           		table2.addCell(getCellHead("Tran date",10,FontConstants.HELVETICA_BOLD,TextAlignment.CENTER,mycolor));
           		table2.addCell(getCellHead("Terminal ID",10,FontConstants.HELVETICA_BOLD,TextAlignment.CENTER,mycolor));
           		table2.addCell(getCellHead("Ref No.",10,FontConstants.HELVETICA_BOLD,TextAlignment.CENTER,mycolor));
           		table2.addCell(getCellHead("Transaction Amount",10,FontConstants.HELVETICA_BOLD,TextAlignment.CENTER,mycolor));
           		table2.addCell(getCellHead("MSC Fee",10,FontConstants.HELVETICA_BOLD,TextAlignment.CENTER,mycolor));
           		table2.addCell(getCellHead("Net Amount",10,FontConstants.HELVETICA_BOLD,TextAlignment.CENTER,mycolor));
        
           		//Query for merchant transaction settlement.
           		String merchant_transaction = "Provide Query for Merchant Transaction Details from provided dates based on merchant id";
   	    		PreparedStatement s2 = conn.prepareStatement(merchant_transaction);
			ResultSet rs2 = s2.executeQuery();
			while(rs2.next()) {
				String No = String.valueOf(rs2.getInt("sn")); //String.valueOf(int) preserves the integer value(no conversional display)
				if (rs2.wasNull()) No = null; //valueOf(null) returns 0, so if null, then set null.
				table2.addCell(getCellBody(No,9,FontConstants.HELVETICA,TextAlignment.CENTER));
				table2.addCell(getCellBody(rs2.getString("MERCHANT_NAMEDTL"),9,FontConstants.HELVETICA,TextAlignment.CENTER));
				table2.addCell(getCellBody(rs2.getString("CARD_TYPE"),9,FontConstants.HELVETICA,TextAlignment.LEFT));
				table2.addCell(getCellBody(rs2.getString("CARD_NUMBER"),9,FontConstants.HELVETICA,TextAlignment.LEFT));
				table2.addCell(getCellBody(rs2.getString("TRAN_DATE"),9,FontConstants.HELVETICA,TextAlignment.CENTER));
				table2.addCell(getCellBody(rs2.getString("TERMINAL_ID"),9,FontConstants.HELVETICA,TextAlignment.CENTER));
				table2.addCell(getCellBody(rs2.getString("REF_NUMBER"),9,FontConstants.HELVETICA,TextAlignment.LEFT));
				
				String Transaction_Amount = String.valueOf(decimalFormat.format(rs2.getDouble("TRANSACTION_AMOUNT")));
		        	if (rs2.wasNull()) Transaction_Amount = null;
				table2.addCell(getCellBody(Transaction_Amount,9,FontConstants.HELVETICA,TextAlignment.RIGHT));
				
				String MSC_Fee = String.valueOf(decimalFormat.format(rs2.getDouble("MSC_FEE")));
		        	if (rs2.wasNull()) MSC_Fee = null;
				table2.addCell(getCellBody(MSC_Fee,9,FontConstants.HELVETICA,TextAlignment.RIGHT));
				
		        	String Net_Amount = String.valueOf(decimalFormat.format(rs2.getDouble("NET_AMOUNT")));
		        	if (rs2.wasNull()) Net_Amount = null;
		        	table2.addCell(getCellBody(Net_Amount,9,FontConstants.HELVETICA,TextAlignment.RIGHT));
			    } //End of Merchant Transaction Settlement Query
		        rs2.close();
		        s2.close();
		
   	        	//Add all the table content to document.
   	        	doc.add(table2);
            		doc.add(new Paragraph("*****  This is the System generated Advice. Hence no signature is required.").setPaddingLeft(20).setFontSize(9));
            		doc.close();
        		try {
				EmailPdf.sendMail(filename,"luffy92dragon@gmail.com"); //Send Mail.
		   	    } catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		            }
	   	} //End of Max net amount if-condition.
	 } //End of Merchant Details Query.
         rs.close();
	 s.close();
	 conn.close();
        } catch (SQLException e) {
          e.printStackTrace();}
   } 
	
private static Cell getCell(int rowspan,int colspan,String para,String font,int font_size,int padding_space,Color color,TextAlignment alignment,int height) throws IOException {
      Cell cell = new Cell(rowspan,colspan);
      if(para!=null) { 
      Paragraph p = new Paragraph(para); //if para=null gives error.
      p.setFontSize(font_size);
      p.setFont(PdfFontFactory.createFont(font));
      p.setPaddingLeft(padding_space);
      p.setFontColor(color);
      p.setTextAlignment(alignment);
      p.setHeight(height);
      cell.add(p);
      cell.setPaddingBottom(0);
      cell.setPaddingTop(0);
      }
      return cell;
   }
    
private static Cell getCellHead(String para,int font_size,String font,TextAlignment alignment,Color color) throws IOException {
    	Cell cell = new Cell();
    	Paragraph p = new Paragraph(para); 
    	p.setFontSize(font_size);
    	p.setFont(PdfFontFactory.createFont(font));
    	p.setTextAlignment(alignment);
    	cell.add(p);
    	cell.setBackgroundColor(color);
    	cell.setPaddingLeft(0);
        cell.setPaddingRight(0);
        cell.setPaddingTop(0);
        cell.setPaddingBottom(0);
        return cell;
    }
      
private static Cell getCellBody(String para,int font_size,String font,TextAlignment alignment) throws IOException {
    	Cell cell = new Cell();
    	if(para!=null) { 
    	Paragraph p = new Paragraph(para); //gives error,if para is null.
    	p.setFontSize(font_size);
    	p.setFont(PdfFontFactory.createFont(font));
    	p.setTextAlignment(alignment);
    	cell.add(p);
    	cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
    	cell.setPaddingLeft(0);
        cell.setPaddingRight(0);
       }
        return cell;
    }
 }
