package fu.hao.intellidroid.core;

import fu.hao.intellidroid.utils.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class: ManifestAnalysis
 * Description: Process manifest.xml and get relevant declarations.
 * Authors: Hao Fu(haofu@ucdavis.edu)
 * Date: 7/14/2016 5:59 PM
 */
public class ManifestAnalysis {
    private String TAG = getClass().getSimpleName();

    private String manifestPath = "";
    private String packageName = "";
    private String applicationName = "";

    private List<String> activities = new ArrayList<>();
    private List<String> services = new ArrayList<>();
    private List<String> broadcastReceivers = new ArrayList<>();
    private List<String> contentProviders = new ArrayList<>();

    private Map<String, String> actionServiceMap = new HashMap<>();
    private Map<String, List<String>> receiverActionMap = new HashMap<>();
    private String mainActivityName = "";

    public ManifestAnalysis(String manifestPath) {
        this.manifestPath = manifestPath;

        processManifest();
    }

    public String getPackageName() {
        return packageName;
    }

    public void processManifest() {
        try {
            File manifestFile = new File(manifestPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document manifestXML = dBuilder.parse(manifestFile);
            manifestXML.getDocumentElement().normalize();

            Element manifestElement = (Element) manifestXML.getElementsByTagName("manifest").item(0);
            packageName = manifestElement.getAttribute("package");

            Element applicationElement = (Element) manifestElement.getElementsByTagName("application").item(0);
            applicationName = applicationElement.getAttribute("android:name");

            processActivities(manifestXML);
            processServices(manifestXML);
            processProviders(manifestXML);
            processReceivers(manifestXML);
        } catch (Exception e) {
            Log.err(TAG, e);
        }
    }

    private String getFullClassName(String componentName) {
        if (componentName.startsWith(".")) {
            return packageName + componentName;
        } else if (!componentName.contains(".")) {
            return packageName + "." + componentName;
        } else {
            return componentName;
        }
    }

    private void processActivities(Document manifestXML) {
        NodeList activityNodes = manifestXML.getElementsByTagName("activity");

        for (int i = 0; i < activityNodes.getLength(); i++) {
            Node activityNode = activityNodes.item(i);

            if (activityNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element activityElement = (Element) activityNode;
            String activityName = getFullClassName(activityElement.getAttribute("android:name"));
            activities.add(activityName);

            NodeList actionNodes = activityElement.getElementsByTagName("action");

            for (int j = 0; j < actionNodes.getLength(); j++) {
                Element actionElement = (Element) actionNodes.item(j);
                String actionString = actionElement.getAttribute("android:name");

                if (actionString.equals("android.intent.action.MAIN")) {
                    mainActivityName = activityName;
                    Log.debug(TAG, "Manifest main Activity: " + mainActivityName);
                }
            }
        }
    }

    private void processServices(Document manifestXML) {
        NodeList serviceNodes = manifestXML.getElementsByTagName("service");

        for (int i = 0; i < serviceNodes.getLength(); i++) {
            Node serviceNode = serviceNodes.item(i);

            if (serviceNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element serviceElement = (Element) serviceNode;
            String serviceName = getFullClassName(serviceElement.getAttribute("android:name"));
            services.add(serviceName);

            //NodeList intentNodes = serviceElement.getElementsByTagName("intent-filter");
            //if (intentNodes.getLength() == 0) {
            //    continue;
            //}
            //Node intentNode = intentNodes.item(0);

            NodeList actionNodes = serviceElement.getElementsByTagName("action");

            for (int a = 0; a < actionNodes.getLength(); a++) {
                Element actionElement = (Element) actionNodes.item(a);
                String actionString = actionElement.getAttribute("android:name");

                actionServiceMap.put(actionString, serviceName);
            }
        }

        Log.debug(TAG, "===============================================");
        Log.debug(TAG, "Manifest Analysis: Services");
        for (String actionString : actionServiceMap.keySet()) {
            Log.debug(TAG, actionString + ": " + actionServiceMap.get(actionString));
        }
        Log.debug(TAG, "===============================================");
    }

    private void processReceivers(Document manifestXML) {
        NodeList receiverNodes = manifestXML.getElementsByTagName("receiver");

        for (int i = 0; i < receiverNodes.getLength(); i++) {
            Node receiverNode = receiverNodes.item(i);

            if (receiverNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element receiverElement = (Element) receiverNode;
            String receiverName = getFullClassName(receiverElement.getAttribute("android:name"));
            broadcastReceivers.add(receiverName);

            //NodeList intentNodes = receiverElement.getElementsByTagName("intent-filter");
            //if (intentNodes.getLength() == 0) {
            //    continue;
            //}
            //Node intentNode = intentNodes.item(0);

            // actions should be within intent-filter
            NodeList actionNodes = receiverElement.getElementsByTagName("action");

            for (int a = 0; a < actionNodes.getLength(); a++) {
                Element actionElement = (Element) actionNodes.item(a);
                String actionString = actionElement.getAttribute("android:name");

                if (!receiverActionMap.containsKey(receiverName)) {
                    receiverActionMap.put(receiverName, new ArrayList<String>());
                }

                receiverActionMap.get(receiverName).add(actionString);
            }
        }

        Log.debug(TAG, "Manifest Analysis: Receivers");
        for (String receiverName : receiverActionMap.keySet()) {
            Log.debug(TAG, receiverName + ": ");
            for (String actionString : receiverActionMap.get(receiverName)) {
                Log.debug(TAG, "    " + actionString);
            }
        }
        Log.debug(TAG, "===============================================");
    }

    private void processProviders(Document manifestXML) {
        NodeList providerNodes = manifestXML.getElementsByTagName("provider");

        for (int i = 0; i < providerNodes.getLength(); i++) {
            Node providerNode = providerNodes.item(i);

            if (providerNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element providerElement = (Element)providerNode;
            String providerName = getFullClassName(providerElement.getAttribute("android:name"));
            contentProviders.add(providerName);
        }

    }

    public String getAppName() {
        return applicationName;
    }

    public List<String> getActivities() {
        return activities;
    }

    public List<String> getServices() {
        return services;
    }

    public List<String> getBroadcastReceivers() {
        return broadcastReceivers;
    }

    public List<String> getContentProviders() {
        return contentProviders;
    }
}
