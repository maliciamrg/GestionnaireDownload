import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Ssh {

	public static ArrayList<String> executeAction(String command) {
		StringBuffer output = new StringBuffer();
		Param.logger.debug("executeAction:"+command);
		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");

			}

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>(); 
		}

		return (ArrayList<String>) Arrays.asList(output.toString().split("\n"));
	}

}
