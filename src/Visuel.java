
import java.io.*;
import java.text.*;
import org.jfree.chart.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.*;
import org.jfree.util.*;public class Visuel
{
	public static String generate_image_resumer_serie(String serie, String strnbjourprochainepisodes, int nbpresent, int nbencours, int nbabsent, int nbavenir)
	{
		String titre = serie + " " + strnbjourprochainepisodes;
		/*JFreeChart*/
		DefaultPieDataset objDataset = new DefaultPieDataset();
		objDataset.setValue("nbpresent", nbpresent);
		objDataset.setValue("nbencours", nbencours);
		objDataset.setValue("nbabsent", nbabsent);
		objDataset.setValue("nbavenir", nbavenir);
		JFreeChart objChart = ChartFactory.createPieChart3D(
		    titre,   //Chart title
		    objDataset,          //Chart Data 
		    true,               // include legend?
		    true,               // include tooltips?
		    false               // include URLs?
		);
        PieSectionLabelGenerator generator = new StandardPieSectionLabelGenerator(
			"{0} {1}", new DecimalFormat("0"), new DecimalFormat("0.00"));
        PiePlot3D plot = (PiePlot3D) objChart.getPlot();
        plot.setStartAngle(180);
        plot.setDirection(Rotation.CLOCKWISE);
        plot.setForegroundAlpha(0.5f);
        plot.setLabelGenerator(generator);
        int width = 320; /* Width of the image */
        int height = 240; /* Height of the image */ 
		//File pieChart = new File( "PieChart_"+serie+".jpeg" ); 

		//ChartUtilities.writeChartAsPNG( imageString , objChart , width , height );
		BufferedImage objBufferedImage=objChart.createBufferedImage(width, height);
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
		try
		{
			ImageIO.write(objBufferedImage, "png", bas);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		byte[] byteArray=bas.toByteArray();

		String imagestatseriehtml = "<img src='data:image/png;base64," + DatatypeConverter.printBase64Binary(byteArray) + "'>";
		return imagestatseriehtml;
	}
}
