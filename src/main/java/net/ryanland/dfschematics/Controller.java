package net.ryanland.dfschematics;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.ryanland.dfschematics.df.ItemAPIManager;
import net.ryanland.dfschematics.df.code.CodeLine;
import net.ryanland.dfschematics.schematic.DFSchematic;
import net.sandrohc.schematic4j.SchematicLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public static BorderPane root;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button sendRecode;
    @FXML
    private Button sendCodeClient;
    @FXML
    private Button sendBuilderRecode;
    @FXML
    private Button sendBuilderCodeClient;
    @FXML
    private Label recodeStatus;
    @FXML
    private Label codeClientStatus;
    @FXML
    private Button retryButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        versionLabel.setText("v" + properties.getProperty("version"));

        root = borderPane;

        ItemAPIManager.recodeStatus = recodeStatus;
        ItemAPIManager.sendRecode = sendRecode;
        ItemAPIManager.sendBuilderRecode = sendBuilderRecode;
        ItemAPIManager.codeClientStatus = codeClientStatus;
        ItemAPIManager.sendCodeClient = sendCodeClient;
        ItemAPIManager.sendBuilderCodeClient = sendBuilderCodeClient;
        ItemAPIManager.retryButton = retryButton;
    }

    @FXML
    void retryConnections() {
        ItemAPIManager.attemptConnections();
    }

    @FXML
    private Button filePicker;
    @FXML
    private Label fileStatus;

    public static File selectedFile;
    public static DFSchematic schematic;

    @FXML
    void pickFile() {
        // create file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Schematic...");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Downloads"));

        // extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Schematic files (*.schem *.litematic *.schematic)", "*.schem", "*.litematic", "*.schematic");
        fileChooser.getExtensionFilters().add(extFilter);

        // choose file
        File file = fileChooser.showOpenDialog(DFSchematics.stage);
        if (file == null) {
            System.out.println("No file selected");
            return;
        }

        fileStatus.setVisible(true);
        fileStatus.setTextFill(Color.LIGHTBLUE);
        fileStatus.setText("Reading...");

        // file selected
        System.out.println("Selected file: " + file.getAbsolutePath());
        filePicker.setText(file.getName());
        selectedFile = file;
        String format = file.getName().replaceAll("^.*\\.", "");

        // read schematic
        try {
            schematic = new DFSchematic(SchematicLoader.load(file));
        } catch (Exception e) {
            error("Error: " + e.getMessage());
            return;
        }

        ItemAPIManager.attemptConnections();
        if (ItemAPIManager.recodeConnected) sendRecode.setDisable(false);
        if (ItemAPIManager.codeClientConnected && ItemAPIManager.codeClientAuthorized) sendCodeClient.setDisable(false);
        configureButton.setDisable(false);
        success("Successfully loaded (Size: %sx%sx%s)"
            .formatted(schematic.getSchematic().width(), schematic.getSchematic().height(), schematic.getSchematic().length()));
        System.out.println("Loaded Schematic: " + file.getName());
    }

    private void error(String msg) {
        fileStatus.setVisible(true);
        fileStatus.setTextFill(Color.RED);
        fileStatus.setText(msg);
    }

    private void success(String msg) {
        fileStatus.setVisible(true);
        fileStatus.setTextFill(Color.GREEN);
        fileStatus.setText(msg);
    }

    @FXML
    private Button configureButton;

    @FXML
    void configure(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("configuration.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Schematic Configuration");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.getIcons().add(new Image(String.valueOf(DFSchematics.class.getResource("logo.png"))));
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(((Node) event.getSource()).getScene().getWindow());
            stage.setOnCloseRequest(evt -> borderPane.setDisable(false));
            borderPane.setDisable(true);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void sendTemplateToRecode() throws InterruptedException {
        List<CodeLine> codeLines = schematic.getTemplateFactory().generate(true);
        ItemAPIManager.sendTemplatesToRecode(codeLines, selectedFile.getName());
        success("Template" + (codeLines.size() == 1 ? "" : "s") + " sent");
    }

    @FXML
    void sendTemplateToCodeClient() {
        List<CodeLine> codeLines = schematic.getTemplateFactory().generate(true);
        ItemAPIManager.sendTemplatesToCodeClient(codeLines, selectedFile.getName());
        success("Template" + (codeLines.size() == 1 ? "" : "s") + " sent");
    }

    private static final String BUILDER_TEMPLATE = "H4sIAAAAAAAAA+17yRLjxrXlrziqF16wnomRAKqjF5gnYiImAtYLBeZ5nqHQ9/gj3s5f1qiyJFsht4dovWhXh7kggUQi8yR4855zgMR3H8K6i6rpw6fff/ehiD98+tP+h48//H76MCZ9EsxXQTBmV7Wr1pw0P9S/tr6UfD7vy87HD3EwBz/Wukq/o7ulnT+B4UeG+1alrE8wiuEfi/jTNx+aok2iMUjnT/0y9nXy7TQHV1H8bVYH0/RtH7TJNx8+zkH26bu4mPo6OD59pwZN8um3331zNR/URfTNh09pUE/Jx28+zMk+X7vffPjmw/e//f777z98//3HD1PdzR8+Ad9//Gqwgl8RVugrwgp/RViRrwgr+hVhfXxFWLGvCCv+a2JN2jgZv43yZJp/RKcv4YWFWqqqmJ2gXpLp04UwP/pkjJYw+RR1cXL1cI1gTj53ecH8PIRgmfNu/IL5dQTtM2jjq8VvPrTXGL+U/vEP4R//UHNLG81F1/7mj3+A//hf13doXr03wfy5y6K+0Hw5a03G6ap1nQhee5+7/NKGgEwi+cPnfgO9ELyHas7c5GFE49AMdyDroNswQ4iYzxTAgTXj1iOlTL7jGSiLm2ZQuWnIEc0jDem0bXegrSjyIEsikXxf77G1bcs1XlzQaVJGqzU3mml4cAEMhkIJWxHUKbBMKMHSNYy8UvkW6DUuXXkJ2o0bhLqaegPlDWp9bn+61kCM4R6NN7Dnzylt1fkMwTZ8Op20I+ycAB1mKjoFSnbX6E9ugfbWbddII0hBj0VuGzUerdhNRSveak+l6c+hJap1qA6BijWrDfujrxbNBQjEqM6pHq4WHCrVn3WtMxbFY4zm568RZo3pRYh53VnAQHmpqTxYWh4opAIrF+SKSn+g5iahhYFzB9RkHMJggfN8y7lkIuEkPVcHGcewfWA8O3esWL3n4ymnor3ZNUrydW6FD3DKHFxi33kyu3e9cd1d3NoBYYvM5vrb/oaXcBx15ZxNdXUgxtrG4CUXLaPI9+f93tHuc3/nBWg6S29roAmbpj0VlXSK0hi0WUyYrclxyYtYOXvGR8In+xlV+tt7sWUvKkqq7Iyzy2ipqFW+NwW/FLRb92IqpTeaHn+sneV1VfiidLRiaLhmyJA48vytbJ5Pxk52n1gveZTS7CcTZSy+DUCDBeH0Y2kQuK8sZgGJg91b9Q3x4tTMb5o709ONs2DzwhLb8Rch+GxztL6ce/faNZ0RgesldJVZ30KSkenYmtIh5venErCzv8N2f65HxUWiiVmVIBwsarAkzg6wMWrV8zTwXKVMjwhhmJLzwq2LzUzl6nwluQXVuAVcfxvuN70bgsWWBz58dDaw0dE+AQA/dfvsUWS/rQwCSCUOvpoIeHIkWvSUHpR5sbSy5toGr7Xj+prP/or5133M8JIV5f6QFMmTKl+2Rmo0HrQRQX3rZkVQ33xzJADSTlQzeADC8gZ7IX1ztxQ26jPoDsyo3aJBXpsvbnve0nkZo/Roe/joU9iNRDl6TqxYaNwoZGS5JFVp2Ky7r8l7fpYCdvDR7tJsTLZ59fQeSIs8U7L2235oWGBDllULgmTVUA1wmlNdpNbLbtK7sUhPuiafPqXHYTJ5W2Fuj2RSEe6PU0Pk0jaGXdy7MKSMdslbZjM69wUpjqFGb7bJQg199WYSSmwlGtecHPRYRSyZeWUK4Uz3vPbRmsb3as836r0lrGyRTTPlWaZuJH72LzPx7Dw+qXfPcCKuxafk5V0uYRGP++YjY49+OVhJYstkGd/kNoZFxzu3UcobajKd/ekkoh3LrmE7EB0QqL0Q67PyF1q8OaHomd7t0NC6cfkdMOFmBVJQp/oMpupitfDkueX8FD+nnaow0T0Vv8oWwAgp8V660PxiKHbQTzzKTrTpyOQB2cu+CA92WmFlfsb1lqav90BQRnPjsCt2D2+7whNIcV7JepQqRQmNqFnBuN0c6dO/93JAJ1I4oilAvusIMCZWAsEg2EDQuP4RX2Th9U3R1ei6LKFocb5allI9H7iNMo+OW0zfpOsK1uK2UTG73kBPkwYKM3Qc9TJk3axHDcTqAw8EYAifbajh77h+EMkdO2F/vWe4lMB9oMI7drvd78wORH1anncMGdY1vZd7f8T6Cp/7koB4DD8f7u0Nx0u6Qv5SuDdIujXMwjrYe0v3qLU8RNwhc4lrTCpO0bYGK4S0sLq/JWIkPLzghuDu8feG5x8VrtrzrWTdbtHybkK7tJAra1X2aL1BPSDunJgK41GY8TWF0xuSHY+L0vwq15nzAZ+MDUsjiliq5lq84qQuiauDripoq+v4qqZsOUc2AT06fyqcKpQMVFzkIjIZ0QplD0ZVNwehtdJX7PE6FaO+6O9/fVEGH3+pJS7lMH4m5d9f22FXXyz8aR6Xz6LiFypj+cz+9WeV8heF0zwWVTLnY7dk+V+Ud2G6TNFn0v+LwqirfyD9YFiCL9T9k3T5ieovpB//DObHU3+B5s9txcFYffvLBi+98ENTf+PcX572S4Hx/X/+bYlF/H8mseLkc4vU51sM/4S6ekMQFirkSXC2TgJ8CBqoWnB76W3wlc/NDOpoGLKzhBJLbbe5Spae9ZPVy/SFz9rjeC3PAQ5jlk7ZWrRfPsrYc2ATolOoiMeCoE6Lu2grq/CEEVKxp9rohUF8NXkpms2M+3cLfS4RImmiWw6nscDAS36b1ALd0vswE7f1Su3lA9ea83womUQ2740xYkimV5AFxZRdhM2yCr+wOkkqEnmDKSK+2FAA6q4nWfLolBvNPfpOPotCdli3oOChd/emv5Fn6Mo5zHMhPDDgkxkk4PSrrivfknifPE9TPJPN89CKFPy8jQ9b1SQXCxCsNNuNM9h9VPaEVW808WQ6UG3sDU4KfhztV43yNe3u2XyJrTtCoszJ5Ipitcrw6LY3c969g9afms0wYe1KeI3AxymbvoXsRl2ZgiyPiSMJU5YU2zBUdPIaTnIB4/IpS9aq5T4nqi/7tMQqcAO0enmsp0RQzK1xdDZqVb8iVKAvwpeS4uFNCVhltcA4Z/xwrIPUadBjcFVFdKGyM/wh1b19SYo4Q93jHG835SFg6qK2qlo0hXbF3LN7IDujlskzfo/JQ9ncjHlN3sXpqMmwPlC85dUbBMeKWqGl0jmTvIxzt1nbVhqgJ9s9JEKC8hSGuDupUI6dQGlZGX4JKL7thgX5UgG6mu71RIaiZIWDeiKtmERZfvo3rYBHiId1FIwrJ8dunoDik6gDvtRN2jnjmmSvdZqBvlWw+A2MSEANn9T5rm/vrmK7dnwk96BUZ/30Lak3qfsxOvEtx+F7PYS+D3RANTGjHJeT3oWv45JgYc7XaJrm5E0cuUbC9edp01qLh6VUPM5dfa+mfYBuVNfs7u5WSBCqsVc59S4x44aos3/yVwblV9Ic+GAnwLVkH0KQxHfqPrBF7uO1IS0wvJ6af5NmIvO7bp1Yl5x1CQJj+e26HjO2fccMnm96Ve1dwko3t7F8tsBtRUN/XnFKgYWOfFqpChWggCN7HPgIjiGwSQqkaj0khw0hVYwl9Om/EyjETA1bJoIMyOAKXgd4A85SXR6IGd+Q4tnYucKNNYPreoROaL90DoWZfjuqxzgnFQgr8I18dQzr6QtnkOS/GekfxPGzfPz3yAj8VW+o/guwUfiZhvVgnP9hLiIup+/eA4W6nD7IAv1TPsp3GOgFc8lOB4SGlzraA5aamd0kL3G0eshnlvodxXeYqNiDntUrzVt6Ru8ecY+j46an1vHwjgC/FCI05mj8GBfiFpez9MSJeJdOBolZasxEEMrEingjPp8qY1fomcYeLZQXUllJZJBwPSnUmoynYqnyjVWILr1JETxZW/4C3hTXy20+bFlam27fRtGtrxNe8fAepzbb0M5bGDsnlws+BjtN25bCUFeh4MiPCUiTx2Q5BKeJhtzDPePa24yMh8i/dcMBsb6zhnEfKrduWYSgp2xrpOaFxewrZOxqPJ7FWuKsBgew8+5m8nh25OYOQFhbCq8b76wsBZ5XZE5tZV3a9W0WxFvrAI/JFKmeJBMuozXILiyRd2OXLx6GBvDCiHJzNKJGC6QWKxpPsM31g7Qfi30+h/eCQBviIjk0RrpeqOYyB+Lk3SpWN5P0nFu063fGQORbF5msCddFFRrIu483HLBkMxKrwzU5MuTY1DeO9/5cuWEj5Vno+2YrTMpWufMZsXHvmOZJ2RmmTq4nR01scEk2T6rEiybgIF7jPU6niKYSLh/DnojSKDuZVHeHGBH0jvIT8WD4vFNlzoqj+4Ave/P04aSD5CcnLYHvaVQdr8SWMEgwqUdysNXJhUBcoRTDlkDZoOVhdH5S2qdnLJyQGiAhRgzg6lxWLKmmaWKniOATt5tlGQPk1CIdh9OLOW8MwZqsyM4ZCdExZTmqrrqrEWPbBBhLCS6Wt2PA25tLXw2b+DKNXZ0klTMgjp+O+tvIHGzW4abj3RSrG2y1wucBOFIpD/ca1ZFn6fBLcJ+3g7Arjns9RRUEa69z+zIJC8jpORM/8bCmoECtnot8hpsDSTJZryC3p27NS5GKD4ZEvgEIvOH1fT+xdVXmx3U5o4fQjvYq7ykQajN/+RYOYLdRxoZDsT2SfOFNw50nA24J5veWSx81H/ga8wQjOo0pJJ/zW3TGjiIATTucIWWdXMRpga1ohxYB24uFZUx/qOHAPKgaumuPdzMiBDc7mMFK+9Nq3YfR4qaGI+NoosoC8QJxf3OTBUV3g4OjwsJWiv1sjP7tjP5RHvqLTPx3WehXfVT2L8BCUzJ/IWC2nYu5SKZ/mIxw8B3a9/ALGaFubdq6nKMvGrpkJNuaGVc7jEEmSmY+heil6DvSRDS0sPh8byUelCrsYfj5GwP1Hifu5XJljBhHxAnrtZalmvs7p8/K59fqUpBqTCJsZczT27SrWaQeFQSCIaFUdcdtwysa4sqL6M6GZFXG2LVRZKFiybR3G/46SpaFVR17XwjOZUZIZXtTHqcah+RLtQ1xe80fSOfN4CSTfHpIKmccAkjmPdpJbjSFmQTRM4Yol5Mr+XIo4w4EaGoi/HoGCONuyV4QD3vkP17Kps37jYArxa+pDmVoM+hhuSOp7hDklmt5cV/CE8wXv5QuT8B1Z3QTQdcX71QTFqMDWgrJDg1DZFWDUW3dBm8/gd4a+4SaMT91v30S40Y9aXfzFzX15CJapnfW2A1zvk4Bb2SPB1pSCmTaB9cxCULU9eODil4c4oTCjcLFN/lCJRQ8aD8SPKewl12IA7kz2Bx8S4HbcWkxOMbQmaRClGwhG9Qe02Sl+Xgoy+A5OcGDHYVWn+9upbjGsLbeIhiTUBBnu6NFV19dPkHYH/xVB4vTpGAbJal6LjFqd1HRI9Yn686eWUHIIaf5BkkNefc1A7FiGrBMbLWpw5+0tfF6IB6xAzCm7U2nOZ9PG0j3SL7Z7KAdeJBh07j7KmyEvhd3lCZKFGgirlWOmhC9jXPx+DvoKMUzbMdrRiE2hCeQKnDCbem7dUVaBYael0EFwE5/vL2QR+8j0QZrGN+SaBe1xe1uS/xG9hsFREz0UkPd5mGPb4l1FWBXwJeAwmi9hCr9PrXPFUZK+VipmYCebTELd0eaHzp62rXjog2nyn7UaWhTvke84pEWux9xfzMsWdW0weMVNMcZDAbubEECYQCEUNoQqPYCrAq/DcYOwTnZLgP70sSdJwVB8Z1GK43E5sR7hrkeNszWa604F4fiDmHUIbYxq8NV08zhvjwQLb9MpQ0AI9PWwpmmRO0+wLC0QRhOhyDFvbv4ph8qHzO2/u9bav8Ecfwyef5d/vhVly/8C/IH9E89t/zRzaB8bQZUDVRSrwLvIAek1GnqWQeZ+QC7vELzDAzZEyJq0kkx7Cx4FpeeBv6UFsfCX+0NbrEexaI83XPSVxIRlxfrlrC6+9JAoNTuMwhYTCMUx85opNGql5hlMcaGun0+nom0HdrNyUvvZiAST7pVFtgZCOriqPAWl7NPKfDEdyI2pgixPd9keaToNZ/0cnVJ0aPhVS/rpwMmSbolCnOvXIkW73HpFTinQBRReiqt8jIfm1fZIFPvVxzLyTQSb7BAJjWJJzk1tNJ2Dz/DBhSsyq1SoKIqtIcX3eLIBiApGNhLMjrXl3Dn0CAc/C1xC+momMXf5PDYxV3rZEguWmN/oevSBclSPGuo9Tginvn7Iz7QF56k+rza8Y14mUy7SxQ6ODdGWAeRWoFY2jzHym7gOIpN1XANPQG1loAJy/q+TbtoTKO4faTEjYqYVoc0YzP7XT7e4rg7yxk3b0iYKqh7cVLKA6yrsnT9rIwurHdPfmA1OUuF2TuBFwcLMkUNF7MNjNxIL3fFVH7eQRRK94XU1Oz2rmmTuztHTeuEOWoaQK2NFjfoawGIAY05fSsoDBLtGLaq9ZlF5AjFBbHBK3gKfLbU9rlp3MmIi33jraR5hXyMZuLuHjRQZLkAHWTEvYTAxG0fCNibn+yAxb3Q2DQlWvVDrktdeYKDVB3rkMjBkzGvv699mZfZMkdF3fjqHt1pOhnOInTcpzAdvV3wHtPc2vGMZydbjpTZE1PhQD33MYUfZqEGokIFXmjGXjicqc7u3Da7g9H2iqw/cXPBAgfhYT20HCODtObmyPdW7nRSbNJ+bRYiVqwxfKLa7e6eU3+Zb6wxNoh+XtYR1gTEN50hiTBlDt1TWc53vPBC1wmAPkB2y+aEpgSdT7zqdIio980voHWtLj81j4d+CtCdCRViFhGiF2azXTAsffSvUb+Z9Mgmavh83QIuvrsOjlNtJIeBOu88DibU0XLRmaGwez9vrAqsj9AI7lne4JNHKPcb9TB1hBwDWfc0xfk3ufxfkAv0D7DL17TgDPyaVpyBv+qSsytWl2hexuTbtSviXyB8dmPy6fe//Qdi79ecCVt+DeFLCP7lVPzz8WwMjp9H6Ar+Dv7dXw3Lj//vwP+r4PjrF/F/UCyH0cDPkWbFnC/h76Kuuf8ovphkvTPcT49s/6qy/M+P/+dQ/ptXxvpB//3G+VGm/SLcf91Vi1dUXzo1T4KfYt2slrrWtjYZP30nxp9+L/5PHEKvD4YTH/8D/LwBEyiKXNsYAeMYDlyt/gcBYghEPAj8h5H/TKvqY3dp3M+J8tN3n0d6za7puvhmkbXB552r9sjOapTaezhNwBsjzpdniOTV+GswpHsb7KJ+WEtkN07ZZ9uorUPgRX273+4JqCH22577kqL3I0ItFMWPANFMIUblhdV3qCmCMm6825lVrXlbGaEeLTT1NKSBAieDpLtfP2SAUEVXnR7PR6WCNObuY+ODqY+MBPvciOLpdU+LjZpEfmXIvZAxty6SgwsEa99j/oVk4f0YyiyDg3ZxqtkMnQ4SWdGlS3IqAMnw3wBXHju1UQpllbuiOylQvjaDoxqwkZawMMCzW28BtqdF7gVzszjr0dpm9VCALHOd54lMymQt20syy1p9pewsZRR6r8mwx6d8FegOOBHHoMZUFQPML/fnewuo9snxhw1OmTHe/AfxHo7ZJehw6GOWoaP9SLaqX6PQyAk79TbYknoIqixalyCyKJ5FGaY3fqZTaJmwPEzb7dUrwv1VxHa3xVt1Q5As0MgVz/fUoWP+LsdWVwODISzjVuy1eo/h1jCDsls0BECCUR7iAHOWRWhn8Macrh1ZVl88di4qBTe4MiwwJs82VpFipFPXLolLtrTvW29Jqo+t+k18kblEcIa13BHrSRLW2N86XFJGjtDn3hLOlenMblTV4BXT1QGN0/yQukRT/Vy45xp91ldkjfaSFwmyJJWAotDDGDXRFNlV4GWQMRXmeBEHfFICwYCOm9Scz6VJ8uiz3ldWOLYNSLlk0scv9u0K22TrMpGWgMAF6wh+5eGbLESmyxQr2pRSBNQyAjXGQNVjk0WaLCJBWv2mnny7rsSCfIi0eCqWAviNCPg8e6qMDXinU2uWvStnBXunVH7ZLsXTN8WJLshMbKkjhPw+5B3Nu/r9UzuSmbjcYvHc4tM/1lN7H0LzWHAO35Hq6O30UeP8qV/hdcSu/UO9V50IL/A6dv7p2PQZ6+dxWeblMn5e9rm+c4S0mGkFWQTCC4iYbn3Cf27j2YB9eE3Uy0U0ly1Z/LexxryDfMFh4sc13tN3bVhl2EOz2FNxpUJhXqXavCqVdwrVMiCNVzaF9zb1quedn6/Pq/b56+IzBqAwEexZNqSeNqhZdalaVC5m3RdsqXH9CsBl2b7cY//P7/+anP0bGflI6rrbfkESPxe9PyXrP6e4nzOAEsTJb8LjN38leX9Ny7jBX3Ud93832F91RdR/94syX9MbSNBX9QrS1/QOEvQ1eULoa/KE0Nf0GhL0uIjq44fgy62NC9NP1uI3PywH/Uxk/xuAuLZ8mDkAAA==";

    @FXML
    void sendBuilderTemplateToRecode() throws InterruptedException {
        ItemAPIManager.sendRawTemplatesToRecode(List.of(BUILDER_TEMPLATE), "DFSchematics Builder");
        success("Template sent");
    }

    @FXML
    void sendBuilderTemplateToCodeClient() {
        ItemAPIManager.sendRawTemplatesToCodeClient(List.of(BUILDER_TEMPLATE), "DFSchematics Builder");
        success("Template sent");
    }

    @FXML
    void viewInstructions() {
        DFSchematics.hostServices.showDocument("http://github.com/RyanLandDev/DFSchematics#how-to-use");
    }

    @FXML
    private Label versionLabel;

    @FXML
    void onVersionClick() {
        DFSchematics.hostServices.showDocument("http://github.com/RyanLandDev/DFSchematics");
    }

    @FXML
    void onVersionEnter() {
        versionLabel.setUnderline(true);
    }

    @FXML
    void onVersionExit() {
        versionLabel.setUnderline(false);
    }
}