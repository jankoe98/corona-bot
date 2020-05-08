import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.stream.Stream;

public class CoronaBot extends TelegramLongPollingBot {

    private String[] STATES = {"Baden-Württemberg", "Bayern", "Berlin", "Brandenburg", "Bremen", "Hamburg", "Hessen", "Mecklenburg-Vorpommern", "Niedersachsen", "Nordrhein-Westfalen", "Rheinland-Pfalz", "Saarland", "Sachsen", "Sachsen-Anhalt", "Schleswig-Holstein", "Thüringen"};

    @Override
    public void onUpdateReceived(Update update) {
        //Send Message:
        SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());
        String givenCountry = update.getMessage().getText();
        String cases = null;
        System.out.println(givenCountry);
        if(update.getMessage().getText().equals("/start")){
            try {
                sendMessage.setText("You can start with sending me a message with the country or state you're interested in!");
                sendMessage(sendMessage);
                return;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        if (checkIfState(givenCountry)) {
            try {

                cases = getGermanStateNumber(transformToIso(givenCountry.toLowerCase()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                sendMessage.setText(capitalize(givenCountry) + " hat laut dem RKI" + cases + "Covid19 Fälle.");
                sendMessage(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            try {
                cases = getCaseNumber(givenCountry);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                sendMessage.setText(cases);
                sendMessage(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private static String capitalize(String str){

        if(str == null || str.length() == 0)
            return "";

        if(str.length() == 1)
            return str.toUpperCase();

        return str.substring(0, 1).toUpperCase() + str.substring(1);

    }

    @Override
    public String getBotUsername() {
        return "coronaNumberBot";
    }

    public String transformToIso(String state){

        switch (state){
            case "baden-württemberg": return "DE-BW";
            case "bayern": return "DE-BY";
            case "berlin": return "DE-BE";
            case "brandenburg": return "DE-BB";
            case "bremen": return "DE-HB";
            case "hamburg": return "DE-HH";
            case "hessen": return "DE-HE";
            case "mecklenburg-vorpommern": return "DE-MV";
            case "niedersachsen": return "DE-NI";
            case "nordrhein-westfalen": return "DE-NW";
            case "rheinland-pfalz": return "DE-RP";
            case "saarland": return "DE-SL";
            case "sachsen": return "DE-SN";
            case "sachsen-anhalt": return "DE-ST";
            case "schleswig-holstein": return "DE-SH";
            case "thüringen": return "DE-TH";

            default:
                throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    @Override
    public String getBotToken() {
        return "1223431810:AAEkKLwOq32-ucIJc0yAjDTqfwNDU7151_g";
    }

    public boolean checkForCountry(String message) {

        return false;
    }

    public boolean checkIfState(String state){
           for(int i = 0; i<STATES.length; i++){
               if(state.equalsIgnoreCase(STATES[i])) return true;
           }
           return false;
    }

    public String getUsNumber() throws IOException {
        String uscases = null;
        URL obj = new URL("https://api.apify.com/v2/key-value-stores/moxA3Q0aZh5LosewB/records/LATEST?disableRedirect=true");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            //System.out.println(response);
            in.close();
            int index = response.toString().indexOf("totalCases");
            String now = response.substring(index);
            int endIndex = now.indexOf(",");
            String cases = now.substring(index, endIndex);
            uscases = cases.substring(cases.indexOf(":") + 1);

        } else {
            System.out.println("GET Response not available");
        }
        return uscases;
    }

    public String getCaseNumber(String country) throws IOException {
        String number = null;
        if (country.equals("united-states") || country.equals("us") || country.equals("US") || country.equals("United-States")) {
            number = getUsNumber();
        }

        else {
            String url = "https://api.covid19api.com/country/" + country + "/status/confirmed/live";
            System.out.println(url);
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            int responseCode = con.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                //System.out.println(response);
                in.close();
                int index = response.toString().lastIndexOf("{");
                String now = response.substring(index);
                int endIndex = now.indexOf("Status") - 6;
                int indexCases = now.indexOf("Cases");
                String cases = now.substring(indexCases, endIndex);
                number = cases.substring(cases.indexOf(":") + 1);


            } else {
                System.out.println("GET Response not available");
            }
        }
        return capitalize(country) + " has " + number + " Covid19 cases.";

    }

    public String getGermanStateNumber(String state) throws IOException {
        String url = "https://covid19-germany.appspot.com/timeseries/" + state + "/" + "cases";
        System.out.println(url);
        String number = null;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            //System.out.println(response);
            in.close();
            int index = response.toString().lastIndexOf("]")-5;
            String now = response.substring(0, index);
            int endIndex = now.lastIndexOf(":")+ 1;
            String cases = now.substring(endIndex, index);
            number = cases;


        } else {
            System.out.println("GET Response not available");
        }
        return number;
    }


}
