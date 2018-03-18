
package org.iaas.transformationanalyzer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Analyses the transformation capabilities
 */
public class Analyzer
{
    private Environment oldEnvironment;
    private Environment newEnvironment;
    private Communication oldCommunication;
    private List<Communication> oldEnvironmentSupportedComms;
    private List<Communication> newEnvironmentSupportedComms;

    Analyzer(Environment oldEnvironment, Environment newEnvironment, Communication oldCommunication, List<Communication> oldEnvironmentSupportedComms, List<Communication> newEnvironmentSupportedComms)
    {
        this.oldEnvironment = oldEnvironment;
        this.newEnvironment = newEnvironment;
        this.oldCommunication = oldCommunication;
        this.newEnvironmentSupportedComms = oldEnvironmentSupportedComms;
        this.newEnvironmentSupportedComms = newEnvironmentSupportedComms;
    }


    /**
     * Gets the result of the analysis and returns to the servlet
     * @return
     */
    public JSONArray GetAnalysisResult()
    {
        // Set the result. Make a Json Array so that the order will be preserved.
        // Simple Json object does not preserve the order of the contents.
        JSONArray jsonArrayResult = new JSONArray();

        // Find whether the transformation is required or not
        TransformationRequiredType transformationRequiredType = getTransformationRequiredOrNot();
        JSONObject requirementJson = new JSONObject().put("Is required?", transformationRequiredType.getValue());
        jsonArrayResult.put(requirementJson);

        // Transformation is required, so find whether it is possible or not
        if(transformationRequiredType == TransformationRequiredType.TRANSFORMATION_REQUIRED)
        {
            List<AnalysisResult> analysisResults = setAnalysisResult();
            AnalysisResult suggestedCommunicationResult = getSuggestedCommunicationType(analysisResults);

            if(suggestedCommunicationResult != null)
            {
                // For debugging purposes, no delete
                /*
                JSONObject diffPointJson = new JSONObject().put("Points", suggestedCommunicationResult.getDifficultyPoint());
                jsonArrayResult.put(diffPointJson);

                JSONObject diffPointHist= new JSONObject().put("History", String.join(" - ", suggestedCommunicationResult.getDifficultyCalcHistory()));
                jsonArrayResult.put(diffPointHist);
                */

                TransformationPossibility transformationPossibility = suggestedCommunicationResult.getPossibility() ? TransformationPossibility.TRANSFORMATION_POSSIBLE : TransformationPossibility.TRANSFORMATION_NOT_POSSIBLE;
                JSONObject possibilityJson = new JSONObject().put("Is possible?", transformationPossibility.getValue());
                jsonArrayResult.put(possibilityJson);

                // Transformation is possible, so find the suggested type and get the find possible losses
                if(transformationPossibility == TransformationPossibility.TRANSFORMATION_POSSIBLE)
                {
                    // get the suggested type and difficulty
                    String suggestedCommunicationType = suggestedCommunicationResult.getName();
                    JSONObject suggestedJson = new JSONObject().put("Suggested communication type", suggestedCommunicationType);
                    jsonArrayResult.put(suggestedJson);

                    // get difficulty
                    String difficulty = suggestedCommunicationResult.getDifficulty();
                    JSONObject difficultyJson = new JSONObject().put("Difficulty", difficulty);
                    jsonArrayResult.put(difficultyJson);

                    // get the possible loss
                    String possibleLoss = String.join(", ", suggestedCommunicationResult.getPossibleLosses());
                    JSONObject getPossibleLossJson = new JSONObject().put("Possible losses", possibleLoss);
                    jsonArrayResult.put(getPossibleLossJson);
                }

                // Transformation is not possible
                else
                {
                    // Do nothing here, see the paper for details and why it is not possible.
                }
            }
        }

        // Transformation is required for application level. There might be possible loss.
        else if(transformationRequiredType == TransformationRequiredType.TRANSFORMATION_REQUIRED_ONLY_APP_LEVEL)
        {
            List<AnalysisResult> analysisResults = setAnalysisResult();
            AnalysisResult suggestedCommunicationResult = getSuggestedCommunicationType(analysisResults);

            if(suggestedCommunicationResult != null)
            {
                JSONObject diffPointJson = new JSONObject().put("Points", suggestedCommunicationResult.getDifficultyPoint());
                jsonArrayResult.put(diffPointJson);

                // get the possible loss
                JSONObject getPossibleLossJson = new JSONObject().put("Possible losses", suggestedCommunicationResult.getPossibleLosses());
                jsonArrayResult.put(getPossibleLossJson);
            }
        }

        // Transformation is not required, no need to give further explanation
        else
        {
            // There is nothing to do here.
        }

        return  jsonArrayResult;
    }

    /**
     * Finds out that whether the transformation is required or not
     * @return
     */
    private TransformationRequiredType getTransformationRequiredOrNot()
    {
        boolean baseTypeFound = false;
        for (Communication supportedCommItem : this.newEnvironmentSupportedComms)
        {
            if(oldCommunication.getBaseType().equals(supportedCommItem.getBaseType()) && !oldCommunication.getBaseType().equals("NEW"))
            {
                baseTypeFound = true;
                // if there is a 100% same protocol, then there is no need for transformation
                if(communicationEqualivent(oldCommunication, supportedCommItem))
                {
                    return TransformationRequiredType.TRANSFORMATION_NOT_REQUIRED;
                }
            }
        }

        // if there is no same protocol, then check if there is a same base type and return accordingly
        return baseTypeFound ? TransformationRequiredType.TRANSFORMATION_REQUIRED_ONLY_APP_LEVEL : TransformationRequiredType.TRANSFORMATION_REQUIRED;
    }

    /**
     * Sets the analysis result for each communication type
     * Is it possible, difficulty, possible losses
     *
     * @return
     */
    private List<AnalysisResult> setAnalysisResult()
    {
        List<AnalysisResult> results = new ArrayList<AnalysisResult>();
        Communication oldComm = this.oldCommunication;

        for (Communication newComm: newEnvironmentSupportedComms)
        {
            AnalysisResult res = new AnalysisResult();
            List<String> diffCalcHist = new ArrayList<String>();

            // initial difficulty points
            int diffPoint = 0;
            boolean analyzingNewType = false;
            if(oldComm.getBaseType().equals(BaseType.BASE_TYPE_NEW.getValue()) || newComm.getBaseType().equals(BaseType.BASE_TYPE_NEW.getValue()))
            {
                // if one of them NEW type
                // List will contain one element for sure
                if(oldComm.getCommunicationType().size() == 1 && newComm.getCommunicationType().size() == 1)
                    diffPoint = difficultyPointsOfUnknownTypes(oldComm.getCommunicationType().get(0).getItemName(), newComm.getCommunicationType().get(0).getItemName());
                analyzingNewType = true;
            }
            else
            {
                // both are customized from a base type
                diffPoint = difficultyPointsOfKnownTypes(oldComm.getBaseType(), newComm.getBaseType());
                analyzingNewType = false;
            }

            // To see the history of how the difficulty is calculated. Mainly used for debugging
            diffCalcHist.add("Comm: " + newComm.getName());
            diffCalcHist.add("Start: " + diffPoint);

            // Compare Comm Pattern
            List<String> commPatternDiff = findDropDownListsDifferences(oldComm.getCommunicationPattern(), newComm.getCommunicationPattern(), DropDownTypes.DROP_DOWN_COMMUNICATION_PATTERN);
            // Increase the difficulty if some patterns do not exists in the new one
            int commPatternAdded = DifficultyPointsForDropDowns.DIFFICULTY_POINTS_COMM_PATTERN.getValue() * commPatternDiff.size();
            diffPoint += commPatternAdded;
            diffCalcHist.add("CommPattern: " + commPatternAdded);

            // Compare RequestType
            List<String> reqTypeDiff = findDropDownListsDifferences(oldComm.getRequestType(), newComm.getRequestType(), DropDownTypes.DROP_DOWN_REQUEST_TYPE);
            // Increase the difficulty if some request types do not exists in the new one
            int reqTypeAdded = reqTypeDiff.size() > 0 ? DifficultyPointsForDropDowns.DIFFICULTY_POINTS_REQ_TYPE.getValue() : 0;
            diffPoint += reqTypeAdded;
            diffCalcHist.add("ReqType: " + reqTypeAdded);

            // Compare PayloadType
            List<String> payloadTypeDiff = findDropDownListsDifferences(oldComm.getPayloadType(), newComm.getPayloadType(), DropDownTypes.DROP_DOWN_PAYLOAD_TYPE);
            int payloadTypeAdded = reqTypeDiff.size() > 0 ? DifficultyPointsForDropDowns.DIFFICULTY_POINTS_PAYLOAD_TYPE.getValue() : 0;
            diffPoint += payloadTypeAdded;
            diffCalcHist.add("PayloadType: " + payloadTypeAdded);

            // Compare HeaderSize
            int headerSizeAdded = 0;
            if(dropDownListContainsValue(oldComm.getHeaderSize(), HeaderSize.HEADER_SIZE_LARGE.getValue()))
            {
                headerSizeAdded =  DifficultyPointsForDropDowns.DIFFICULTY_POINTS_HEADER_SIZE.getValue() * 3;
            }
            else if(dropDownListContainsValue(oldComm.getHeaderSize(), HeaderSize.HEADER_SIZE_MEDIUM.getValue()))
            {
                headerSizeAdded =  DifficultyPointsForDropDowns.DIFFICULTY_POINTS_HEADER_SIZE.getValue() * 2;
            }
            else if(dropDownListContainsValue(oldComm.getHeaderSize(), HeaderSize.HEADER_SIZE_SMALL.getValue()))
            {
                headerSizeAdded =  DifficultyPointsForDropDowns.DIFFICULTY_POINTS_HEADER_SIZE.getValue() * 2;
            }
            diffPoint += headerSizeAdded;
            diffCalcHist.add("HeaderSize: " + headerSizeAdded);


            // Now find possible losses
            List<String> possibleLosses = new ArrayList<String>();
            List<String> headerSizeDiff = findDropDownListsDifferences(oldComm.getHeaderSize(), newComm.getHeaderSize(), DropDownTypes.DROP_DOWN_HEADER_SIZE);
            List<String> securityDiff = findDropDownListsDifferences(oldComm.getSecurityType(), newComm.getSecurityType(), DropDownTypes.DROP_DOWN_SECURITY_TYPE);
            List<String> authDiff = findDropDownListsDifferences(oldComm.getAuthenticationType(), newComm.getAuthenticationType(), DropDownTypes.DROP_DOWN_AUTHENTICATION_TYPE);
            List<String> qosDiff = findDropDownListsDifferences(oldComm.getQosType(), newComm.getQosType(), DropDownTypes.DROP_DOWN_QOS_TYPE);


            if(analyzingNewType)
            {
                if(headerSizeDiff.size() > 0)
                    possibleLosses.add("Some header fields");
            }
            else
            {
                // the followings has no header field losses
                boolean stompToAmqp = oldComm.getBaseType().equals(BaseType.BASE_TYPE_STOMP.getValue()) && newComm.getBaseType().equals(BaseType.BASE_TYPE_AMQP.getValue());
                boolean mqttToAmqp = oldComm.getBaseType().equals(BaseType.BASE_TYPE_MQTT.getValue()) && newComm.getBaseType().equals(BaseType.BASE_TYPE_AMQP.getValue());
                boolean coapToHttp = oldComm.getBaseType().equals(BaseType.BASE_TYPE_COAP.getValue()) && newComm.getBaseType().equals(BaseType.BASE_TYPE_HTTP.getValue());
                if(headerSizeDiff.size() > 0 || !(stompToAmqp || mqttToAmqp || coapToHttp))
                    possibleLosses.add("Some header fields");
            }


            if(reqTypeDiff.size() > 0)
                possibleLosses.add("Being asynchronous ");
            if(securityDiff.size() > 0)
                possibleLosses.add("Security");
            if(authDiff.size() > 0)
                possibleLosses.add("Authentication");
            if(qosDiff.size() > 0)
                possibleLosses.add("QoS");


            // set the difficulty level
            String difficulty = "";
            if(diffPoint > DifficultyLevels.DIFFICULTY_LEVEL_NOT_POSSIBLE.getThresholdVal())
                difficulty = DifficultyLevels.DIFFICULTY_LEVEL_NOT_POSSIBLE.getValue();

            else if(diffPoint > DifficultyLevels.DIFFICULTY_LEVEL_VERY_HARD.getThresholdVal())
                difficulty = DifficultyLevels.DIFFICULTY_LEVEL_VERY_HARD.getValue();

            else if (diffPoint > DifficultyLevels.DIFFICULTY_LEVEL_HARD.getThresholdVal())
                difficulty = DifficultyLevels.DIFFICULTY_LEVEL_HARD.getValue();

            else if (diffPoint > DifficultyLevels.DIFFICULTY_LEVEL_MEDIUM.getThresholdVal())
                difficulty = DifficultyLevels.DIFFICULTY_LEVEL_MEDIUM.getValue();

            else if (diffPoint > DifficultyLevels.DIFFICULTY_LEVEL_EASY.getThresholdVal())
                difficulty = DifficultyLevels.DIFFICULTY_LEVEL_EASY.getValue();

            diffCalcHist.add("End: " + diffPoint);

            boolean possibility = diffPoint < DifficultyLevels.DIFFICULTY_LEVEL_NOT_POSSIBLE.getThresholdVal();

            // now fill the result object
            res.setName(newComm.getName());
            res.setDifficultyPoint(diffPoint);
            res.setPossibility(possibility);
            res.setDifficulty(difficulty);
            res.setPossibleLosses(possibleLosses);
            res.setDifficultyCalcHistory(diffCalcHist);
            results.add(res);
        }

        return results;
    }


    /**
     * Finds the suggested communication type that will be used for transformation
     * @return
     */
    private AnalysisResult getSuggestedCommunicationType(List<AnalysisResult> analysisResults)
    {
        // Sort according to the difficulty points
        Collections.sort(analysisResults, new Comparator<AnalysisResult>()
        {
            public int compare(AnalysisResult res1, AnalysisResult res2)
            {
                if(res1.getDifficultyPoint() == res2.getDifficultyPoint())
                    return 0;

                return res1.getDifficultyPoint() < res2.getDifficultyPoint() ? -1 : 1;
            }
        });

        int i = 1;
        for (AnalysisResult analysisResult: analysisResults)
        {
            // For debugging purposes, no delete
            //System.out.println(i + "- " + analysisResult.getName() + " " + analysisResult.getDifficultyPoint());
            i++;
        }

        // find minimum difficulty level
        AnalysisResult min;
        String minDiffLevel;
        if(analysisResults.size() > 0)
        {
            min = analysisResults.get(0);
            minDiffLevel = min.getDifficulty();

            // find the ones which has the same difficulty levels
            List<AnalysisResult> minDiffLevelResSet = new ArrayList<AnalysisResult>();
            for (AnalysisResult analysisResult: analysisResults)
            {
                if(analysisResult.getDifficulty().equals(minDiffLevel))
                    minDiffLevelResSet.add(analysisResult);
            }

            // find the ones which has the minimum losses
            List<AnalysisResult> minLostSet = new ArrayList<AnalysisResult>();
            int minLostCount = 999;
            for (AnalysisResult analysisResult: minDiffLevelResSet)
            {
                if(analysisResult.getPossibleLosses().size() == minLostCount)
                {
                    minLostSet.add(analysisResult);
                }
                else if(analysisResult.getPossibleLosses().size() < minLostCount)
                {
                    minLostCount = analysisResult.getPossibleLosses().size();
                    minLostSet = new ArrayList<AnalysisResult>();
                    minLostSet.add(analysisResult);
                }
            }

            // sort the final set according to the points. difficulty levels and losses are same.
            Collections.sort(minLostSet, new Comparator<AnalysisResult>()
            {
                public int compare(AnalysisResult res1, AnalysisResult res2)
                {
                    if(res1.getDifficultyPoint() == res2.getDifficultyPoint())
                        return 0;

                    return res1.getDifficultyPoint() < res2.getDifficultyPoint() ? -1 : 1;
                }
            });

            return minLostSet.size() > 0 ? minLostSet.get(0) : null;
        }

        return null;
    }


    /**
     *  Checks whether the two communications are equals or not.
     *  If they are, then no need for any transformation, if not, then app level transformation will be required.
     *
     * @param oldComm
     * @param newComm
     * @return
     */
    private boolean communicationEqualivent(Communication oldComm, Communication newComm)
    {
        if(oldComm != null && newComm != null)
        {
            boolean commTypeEqual = dropDownListsEquals(oldComm.getCommunicationType(), newComm.getCommunicationType(), DropDownTypes.DROP_DOWN_COMMUNICATION_TYPE);
            boolean commPatternEqual = dropDownListsEquals(oldComm.getCommunicationPattern(), newComm.getCommunicationPattern(), DropDownTypes.DROP_DOWN_COMMUNICATION_PATTERN);
            boolean reqTypeEqual = dropDownListsEquals(oldComm.getRequestType(), newComm.getRequestType(), DropDownTypes.DROP_DOWN_REQUEST_TYPE);
            boolean headerSizeEqual = dropDownListsEquals(oldComm.getHeaderSize(), newComm.getHeaderSize(), DropDownTypes.DROP_DOWN_HEADER_SIZE);
            boolean payloadTypeEqual = dropDownListsEquals(oldComm.getPayloadType(), newComm.getPayloadType(), DropDownTypes.DROP_DOWN_PAYLOAD_TYPE);

            if(commTypeEqual && commPatternEqual && reqTypeEqual && headerSizeEqual && payloadTypeEqual)
            {
                return true;
            }
        }

        return  false;
    }

    /**
     * Compare the items of the DropDownLists
     *
     * The new dropdown element of the new communication should contain everything from the old
     * It may have additional features. If everything is included in the new one, then they can be thought as equal.
     * @param fromOld dropdown element from old
     * @param fromNew dropdown element from new
     * @return
     */
    private boolean dropDownListsEquals(List<DropDownListItem> fromOld, List<DropDownListItem> fromNew, DropDownTypes type)
    {
        // all the items from old should be in the new one
        if(fromOld != null && fromNew != null && fromOld.size() <= fromNew.size())
        {
            for (DropDownListItem dropDownItemOld : fromOld)
            {
                boolean found = false;
                for (DropDownListItem dropDownItemNew : fromNew)
                {
                    boolean equals = dropDownItemOld.getItemName().equals(dropDownItemNew.getItemName()) && dropDownItemOld.getId() == dropDownItemNew.getId();
                    if(equals)
                    {
                        found = true;
                        break;
                    }
                }

                // item is not found in the new one, so they are not equal.
                if(!found)
                {
                    return false;
                }
            }
            // everything from old, is in new, so return true.
            return true;
        }

        return  false;
    }


    /**
     * Finds the differences between two dropdown elements
     *
     * Returns a list which contains the element names which exists in old but not in new.
     *
     * @param fromOld
     * @param fromNew
     * @return
     */
    private List<String> findDropDownListsDifferences(List<DropDownListItem> fromOld, List<DropDownListItem> fromNew, DropDownTypes type)
    {
        List<String> result = new ArrayList<String>();

        if(type == DropDownTypes.DROP_DOWN_REQUEST_TYPE)
        {
            // if the new one has MIME types, it can be thought that there is no difference in this type.
            if(dropDownListContainsValue(fromNew, RequestType.REQUEST_TYPE_ASYNC.getValue()))
            {
                return result;
            }
        }

        else if(type == DropDownTypes.DROP_DOWN_PAYLOAD_TYPE)
        {
            // if the new one has MIME types, it can be thought that there is no difference in this type.
            if(dropDownListContainsValue(fromNew, PayloadType.PAYLOAD_TYPE_MIME.getValue()))
            {
                return result;
            }
        }

        else if(type == DropDownTypes.DROP_DOWN_QOS_TYPE)
        {
            // if the new one has Complex QoS types, it can be thought that there is no difference in this type.
            if(dropDownListContainsValue(fromNew, QoSLevel.QOS_LEVEL_COMPLEX.getValue()))
            {
                return result;
            }
        }

        else if(type == DropDownTypes.DROP_DOWN_HEADER_SIZE)
        {
            // if the new one has Large size, it can be thought that there is no difference in this type.
            if(dropDownListContainsValue(fromNew, HeaderSize.HEADER_SIZE_LARGE.getValue()))
            {
                return result;
            }
        }

        else if(type == DropDownTypes.DROP_DOWN_REQUEST_TYPE)
        {
            // if the new one has Async type, it can be thought that there is no difference in this type.
            if(dropDownListContainsValue(fromNew, RequestType.REQUEST_TYPE_ASYNC.getValue()))
            {
                return result;
            }
        }

        for (DropDownListItem dropDownItemOld : fromOld)
        {
            boolean found = false;
            for (DropDownListItem dropDownItemNew : fromNew)
            {
                boolean equals = dropDownItemOld.getItemName().equals(dropDownItemNew.getItemName()) && dropDownItemOld.getId() == dropDownItemNew.getId();

                // drop down will contain only one element for sure
                if(type == DropDownTypes.DROP_DOWN_SECURITY_TYPE)
                {
                    // DTLS and TLS can be thought as same. They offer the same functionality
                    boolean dtls_tls = dropDownItemOld.getItemName().equals(SecurityType.SECURITY_TYPE_DTLS.getValue()) && dropDownItemNew.getItemName().equals(SecurityType.SECURITY_TYPE_TLS.getValue());
                    boolean tls_dtls = dropDownItemOld.getItemName().equals(SecurityType.SECURITY_TYPE_TLS.getValue()) && dropDownItemNew.getItemName().equals(SecurityType.SECURITY_TYPE_DTLS.getValue());
                    equals = equals || dtls_tls || tls_dtls;
                }

                else if(type == DropDownTypes.DROP_DOWN_AUTHENTICATION_TYPE && dropDownItemOld.getItemName().equals(AuthenticationType.AUTHENTICATION_TYPE_SIMPLE.getValue()))
                {
                    // If old one is simple and the new one has at least one auth, then this is not a loss or difficulty
                    equals =  fromNew.size() > 0;
                }

                else if(type == DropDownTypes.DROP_DOWN_PAYLOAD_TYPE && dropDownItemOld.getItemName().equals(PayloadType.PAYLOAD_TYPE_TEXT.getValue()))
                {
                    // If old one is text, then this is not a loss or difficulty.
                    equals = true;
                }

                else if(type == DropDownTypes.DROP_DOWN_HEADER_SIZE && dropDownItemOld.getItemName().equals(HeaderSize.HEADER_SIZE_SMALL.getValue()))
                {
                    // If old one is small, then this is not a loss or difficulty.
                    equals = true;
                }

                if(equals)
                {
                    found = true;
                    break;
                }
            }

            // Add to list
            if(!found)
            {
                result.add(dropDownItemOld.getItemName());
            }
        }

        return result;
    }


    /**
     * Returns whether the value exists in the dropdown list
     * @param item
     * @param value
     * @return
     */
    private boolean dropDownListContainsValue(List<DropDownListItem> item, String value)
    {
        for (DropDownListItem elem : item)
        {
            if (elem.getItemName().equals(value)) {
                return true;
            }
        }

        return false;
    }


    //region initial difficulty points for the protocols

    /**
     * If the protocols are not newly created, i.e., if they are from the known protocols,
     * then return the initial difficulty points.
     *
     * The difficulty points are calculated according to the implementation of the communication type.
     *
     * @param typeOld
     * @param typeNew
     * @return
     */
    private int difficultyPointsOfKnownTypes(String typeOld, String typeNew)
    {
        if(typeOld.equals(BaseType.BASE_TYPE_AMQP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_STOMP.getValue()))
            return 275;
        else if(typeOld.equals(BaseType.BASE_TYPE_AMQP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_MQTT.getValue()))
            return 275;
        else if(typeOld.equals(BaseType.BASE_TYPE_AMQP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_HTTP.getValue()))
            return 275;
        else if(typeOld.equals(BaseType.BASE_TYPE_AMQP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_COAP.getValue()))
            return 275;
        else if(typeOld.equals(BaseType.BASE_TYPE_AMQP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_XMPP.getValue()))
            return 450;
        else if(typeOld.equals(BaseType.BASE_TYPE_AMQP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_DDS.getValue()))
            return 750;

        else if(typeOld.equals(BaseType.BASE_TYPE_STOMP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_AMQP.getValue()))
            return 200;
        else if(typeOld.equals(BaseType.BASE_TYPE_STOMP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_MQTT.getValue()))
            return 200;
        else if(typeOld.equals(BaseType.BASE_TYPE_STOMP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_HTTP.getValue()))
            return 200;
        else if(typeOld.equals(BaseType.BASE_TYPE_STOMP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_COAP.getValue()))
            return 200;
        else if(typeOld.equals(BaseType.BASE_TYPE_STOMP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_XMPP.getValue()))
            return 450;
        else if(typeOld.equals(BaseType.BASE_TYPE_STOMP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_DDS.getValue()))
            return 750;

        else if(typeOld.equals(BaseType.BASE_TYPE_MQTT.getValue()) && typeNew.equals(BaseType.BASE_TYPE_AMQP.getValue()))
            return 175;
        else if(typeOld.equals(BaseType.BASE_TYPE_MQTT.getValue()) && typeNew.equals(BaseType.BASE_TYPE_STOMP.getValue()))
            return 175;
        else if(typeOld.equals(BaseType.BASE_TYPE_MQTT.getValue()) && typeNew.equals(BaseType.BASE_TYPE_HTTP.getValue()))
            return 225;
        else if(typeOld.equals(BaseType.BASE_TYPE_MQTT.getValue()) && typeNew.equals(BaseType.BASE_TYPE_COAP.getValue()))
            return 225;
        else if(typeOld.equals(BaseType.BASE_TYPE_MQTT.getValue()) && typeNew.equals(BaseType.BASE_TYPE_XMPP.getValue()))
            return 450;
        else if(typeOld.equals(BaseType.BASE_TYPE_MQTT.getValue()) && typeNew.equals(BaseType.BASE_TYPE_DDS.getValue()))
            return 750;

        else if(typeOld.equals(BaseType.BASE_TYPE_HTTP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_AMQP.getValue()))
            return 200;
        else if(typeOld.equals(BaseType.BASE_TYPE_HTTP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_STOMP.getValue()))
            return 200;
        else if(typeOld.equals(BaseType.BASE_TYPE_HTTP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_MQTT.getValue()))
            return 250;
        else if(typeOld.equals(BaseType.BASE_TYPE_HTTP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_COAP.getValue()))
            return 100;
        else if(typeOld.equals(BaseType.BASE_TYPE_HTTP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_XMPP.getValue()))
            return 450;
        else if(typeOld.equals(BaseType.BASE_TYPE_HTTP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_DDS.getValue()))
            return 750;

        else if(typeOld.equals(BaseType.BASE_TYPE_COAP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_AMQP.getValue()))
            return 200;
        else if(typeOld.equals(BaseType.BASE_TYPE_COAP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_STOMP.getValue()))
            return 200;
        else if(typeOld.equals(BaseType.BASE_TYPE_COAP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_MQTT.getValue()))
            return 250;
        else if(typeOld.equals(BaseType.BASE_TYPE_COAP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_HTTP.getValue()))
            return 100;
        else if(typeOld.equals(BaseType.BASE_TYPE_COAP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_XMPP.getValue()))
            return 450;
        else if(typeOld.equals(BaseType.BASE_TYPE_COAP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_DDS.getValue()))
            return 750;

        else if(typeOld.equals(BaseType.BASE_TYPE_XMPP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_AMQP.getValue()))
            return 450;
        else if(typeOld.equals(BaseType.BASE_TYPE_XMPP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_STOMP.getValue()))
            return 450;
        else if(typeOld.equals(BaseType.BASE_TYPE_XMPP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_MQTT.getValue()))
            return 450;
        else if(typeOld.equals(BaseType.BASE_TYPE_XMPP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_HTTP.getValue()))
            return 450;
        else if(typeOld.equals(BaseType.BASE_TYPE_XMPP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_COAP.getValue()))
            return 450;
        else if(typeOld.equals(BaseType.BASE_TYPE_XMPP.getValue()) && typeNew.equals(BaseType.BASE_TYPE_DDS.getValue()))
            return 450;

        else if(typeOld.equals(BaseType.BASE_TYPE_DDS.getValue()) && typeNew.equals(BaseType.BASE_TYPE_AMQP.getValue()))
            return 750;
        else if(typeOld.equals(BaseType.BASE_TYPE_DDS.getValue()) && typeNew.equals(BaseType.BASE_TYPE_STOMP.getValue()))
            return 750;
        else if(typeOld.equals(BaseType.BASE_TYPE_DDS.getValue()) && typeNew.equals(BaseType.BASE_TYPE_MQTT.getValue()))
            return 750;
        else if(typeOld.equals(BaseType.BASE_TYPE_DDS.getValue()) && typeNew.equals(BaseType.BASE_TYPE_HTTP.getValue()))
            return 750;
        else if(typeOld.equals(BaseType.BASE_TYPE_DDS.getValue()) && typeNew.equals(BaseType.BASE_TYPE_COAP.getValue()))
            return 750;
        else if(typeOld.equals(BaseType.BASE_TYPE_DDS.getValue()) && typeNew.equals(BaseType.BASE_TYPE_XMPP.getValue()))
            return 750;

        return 0;
    }


    /**
     * If the protocols are newly created, i.e., if they are brand new protocols
     * then return the initial difficulty points.
     *
     * The difficulty points are calculated according to the communication type of the communication protocols.
     * Since the same communication types can be implemented in a very different ways, the analysis results of the newly created
     * communication types may not be correct and the results are just guesses.
     *
     * @param typeOld
     * @param typeNew
     * @return
     */
    private int difficultyPointsOfUnknownTypes(String typeOld, String typeNew)
    {
        if(typeOld.equals(CommTypes.COMM_TYPES_CS_BI_DIR.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_CS_ONE_DIR.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_CS_BI_DIR.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_MS_BROKERED.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_CS_BI_DIR.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_MS_NOT_BROKERED.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_CS_BI_DIR.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_DS_BROKERED.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_CS_BI_DIR.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_DS_NOT_BROKERED.getValue()))
            return 500;

        else if(typeOld.equals(CommTypes.COMM_TYPES_CS_ONE_DIR.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_CS_BI_DIR.getValue()))
            return 200;
        else if(typeOld.equals(CommTypes.COMM_TYPES_CS_ONE_DIR.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_MS_BROKERED.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_CS_ONE_DIR.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_MS_NOT_BROKERED.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_CS_ONE_DIR.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_DS_BROKERED.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_CS_ONE_DIR.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_DS_NOT_BROKERED.getValue()))
            return 500;

        else if(typeOld.equals(CommTypes.COMM_TYPES_MS_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_CS_BI_DIR.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_MS_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_CS_ONE_DIR.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_MS_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_MS_NOT_BROKERED.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_MS_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_DS_BROKERED.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_MS_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_DS_NOT_BROKERED.getValue()))
            return 500;

        else if(typeOld.equals(CommTypes.COMM_TYPES_MS_NOT_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_CS_BI_DIR.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_MS_NOT_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_CS_ONE_DIR.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_MS_NOT_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_MS_BROKERED.getValue()))
            return 300;
        else if(typeOld.equals(CommTypes.COMM_TYPES_MS_NOT_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_DS_BROKERED.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_MS_NOT_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_DS_NOT_BROKERED.getValue()))
            return 500;

        else if(typeOld.equals(CommTypes.COMM_TYPES_DS_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_CS_BI_DIR.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_DS_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_CS_ONE_DIR.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_DS_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_MS_BROKERED.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_DS_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_MS_NOT_BROKERED.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_DS_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_DS_NOT_BROKERED.getValue()))
            return 300;

        else if(typeOld.equals(CommTypes.COMM_TYPES_DS_NOT_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_CS_BI_DIR.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_DS_NOT_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_CS_ONE_DIR.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_DS_NOT_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_MS_BROKERED.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_DS_NOT_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_MS_NOT_BROKERED.getValue()))
            return 500;
        else if(typeOld.equals(CommTypes.COMM_TYPES_DS_NOT_BROKERED.getValue()) && typeNew.equals(CommTypes.COMM_TYPES_DS_BROKERED.getValue()))
            return 300;

        return 150;
    }

    //endregion


    //region the string or integer values of the constants.

    private enum DifficultyLevels
    {
        DIFFICULTY_LEVEL_NOT_POSSIBLE,
        DIFFICULTY_LEVEL_VERY_HARD,
        DIFFICULTY_LEVEL_HARD,
        DIFFICULTY_LEVEL_MEDIUM,
        DIFFICULTY_LEVEL_EASY;

        public String getValue()
        {
            switch (this)
            {
                case DIFFICULTY_LEVEL_NOT_POSSIBLE:
                    return "Impossible";
                case DIFFICULTY_LEVEL_VERY_HARD:
                    return "Very hard";
                case DIFFICULTY_LEVEL_HARD:
                    return "Hard";
                case DIFFICULTY_LEVEL_MEDIUM:
                    return "Medium";
                case DIFFICULTY_LEVEL_EASY:
                    return "Easy";
                default:
                    return "";
            }
        }

        public int getThresholdVal()
        {
            switch (this)
            {
                case DIFFICULTY_LEVEL_NOT_POSSIBLE:
                    return 750;
                case DIFFICULTY_LEVEL_VERY_HARD:
                    return 450;
                case DIFFICULTY_LEVEL_HARD:
                    return 300;
                case DIFFICULTY_LEVEL_MEDIUM:
                    return 200;
                case DIFFICULTY_LEVEL_EASY:
                    return 100;
                default:
                    return 0;
            }
        }
    }


    private enum DifficultyPointsForDropDowns
    {
        DIFFICULTY_POINTS_COMM_TYPE,
        DIFFICULTY_POINTS_COMM_PATTERN,
        DIFFICULTY_POINTS_REQ_TYPE,
        DIFFICULTY_POINTS_HEADER_SIZE,
        DIFFICULTY_POINTS_PAYLOAD_TYPE,
        DIFFICULTY_POINTS_SECURITY_TYPE,
        DIFFICULTY_POINTS_AUTHENTICATION_TYPE,
        DIFFICULTY_POINTS_QOS_TYPE;


        public int getValue()
        {
            switch (this)
            {
                case DIFFICULTY_POINTS_COMM_TYPE:
                    return 100;
                case DIFFICULTY_POINTS_COMM_PATTERN:
                    return 50;
                case DIFFICULTY_POINTS_REQ_TYPE:
                    return 25;
                case DIFFICULTY_POINTS_HEADER_SIZE:
                    return 25;
                case DIFFICULTY_POINTS_PAYLOAD_TYPE:
                    return 25;
                case DIFFICULTY_POINTS_SECURITY_TYPE:
                    return 10;
                case DIFFICULTY_POINTS_AUTHENTICATION_TYPE:
                    return 10;
                case DIFFICULTY_POINTS_QOS_TYPE:
                    return 10;
                default:
                    return 0;
            }
        }
    }

    private enum TransformationRequiredType
    {
        TRANSFORMATION_NOT_REQUIRED,
        TRANSFORMATION_REQUIRED_ONLY_APP_LEVEL,
        TRANSFORMATION_REQUIRED;

        public String getValue()
        {
            switch (this)
            {
                case TRANSFORMATION_NOT_REQUIRED:
                    return "It is not required";
                case TRANSFORMATION_REQUIRED_ONLY_APP_LEVEL:
                    return "It is required for only application level, application code must be updated.";
                case TRANSFORMATION_REQUIRED:
                    return "It is required";
                default:
                    return "";
            }
        }
    }

    private enum TransformationPossibility
    {
        TRANSFORMATION_POSSIBLE,
        TRANSFORMATION_NOT_POSSIBLE;

        public String getValue()
        {
            switch (this)
            {
                case TRANSFORMATION_POSSIBLE:
                    return "It is possible";
                case TRANSFORMATION_NOT_POSSIBLE:
                    return "It is not possible";
                default:
                    return "";
            }
        }
    }


    private enum BaseType
    {
        BASE_TYPE_AMQP,
        BASE_TYPE_STOMP,
        BASE_TYPE_MQTT,
        BASE_TYPE_HTTP,
        BASE_TYPE_DDS,
        BASE_TYPE_COAP,
        BASE_TYPE_XMPP,
        BASE_TYPE_NEW;

        public String getValue()
        {
            switch (this)
            {
                case BASE_TYPE_AMQP:
                    return "AMQP";
                case BASE_TYPE_STOMP:
                    return "STOMP";
                case BASE_TYPE_MQTT:
                    return "MQTT";
                case BASE_TYPE_HTTP:
                    return "HTTP";
                case BASE_TYPE_DDS:
                    return "DDS";
                case BASE_TYPE_COAP:
                    return "CoAP";
                case BASE_TYPE_XMPP:
                    return "XMPP";
                case BASE_TYPE_NEW:
                    return "NEW";
                default:
                    return "";
            }
        }
    }

    private enum DropDownTypes
    {
        DROP_DOWN_COMMUNICATION_TYPE,
        DROP_DOWN_COMMUNICATION_PATTERN,
        DROP_DOWN_REQUEST_TYPE,
        DROP_DOWN_HEADER_SIZE,
        DROP_DOWN_PAYLOAD_TYPE,
        DROP_DOWN_PAYLOAD_SIZE,
        DROP_DOWN_SECURITY_TYPE,
        DROP_DOWN_AUTHENTICATION_TYPE,
        DROP_DOWN_QOS_TYPE,
        DROP_DOWN_IOT_TYPE;
    }

    private enum CommTypes
    {
        COMM_TYPES_CS_ONE_DIR,
        COMM_TYPES_CS_BI_DIR,
        COMM_TYPES_MS_BROKERED,
        COMM_TYPES_MS_NOT_BROKERED,
        COMM_TYPES_DS_BROKERED,
        COMM_TYPES_DS_NOT_BROKERED;

        public String getValue()
        {
            switch (this)
            {
                case COMM_TYPES_CS_ONE_DIR:
                    return "Client/Server (One-directional)";
                case COMM_TYPES_CS_BI_DIR:
                    return "Client/Server (Bi-directional)";
                case COMM_TYPES_MS_BROKERED:
                    return "Brokered Messaging";
                case COMM_TYPES_MS_NOT_BROKERED:
                    return "Brokerless Messaging";
                case COMM_TYPES_DS_BROKERED:
                    return "Data-Centric Brokered Messaging";
                case COMM_TYPES_DS_NOT_BROKERED:
                    return "Data-Centric Brokerless Messaging";
                default:
                    return "";
            }
        }
    }

    private enum CommPattern
    {
        COMM_PATTERN_FF,
        COMM_PATTERN_RR,
        COMM_PATTERN_PS,
        COMM_PATTERN_MC;

        public String getValue()
        {
            switch (this)
            {
                case COMM_PATTERN_FF:
                    return "Fire/Forget";
                case COMM_PATTERN_RR:
                    return "Request/Response";
                case COMM_PATTERN_PS:
                    return "Publish/Subscribe";
                case COMM_PATTERN_MC:
                    return "Multicast";
                default:
                    return "";
            }
        }
    }


    private enum RequestType
    {
        REQUEST_TYPE_SYNC,
        REQUEST_TYPE_ASYNC;

        public String getValue()
        {
            switch (this)
            {
                case REQUEST_TYPE_SYNC:
                    return "Blocking (Sync.)";
                case REQUEST_TYPE_ASYNC:
                    return "Not Blocking (Async.)";
                default:
                    return "";
            }
        }

    }

    private enum HeaderSize
    {
        HEADER_SIZE_SMALL,
        HEADER_SIZE_MEDIUM,
        HEADER_SIZE_LARGE;

        public String getValue()
        {
            switch (this)
            {
                case HEADER_SIZE_SMALL:
                    return "Small";
                case HEADER_SIZE_MEDIUM:
                    return "Medium";
                case HEADER_SIZE_LARGE:
                    return "Large";
                default:
                    return "";
            }
        }
    }

    private enum PayloadType
    {
        PAYLOAD_TYPE_TEXT,
        PAYLOAD_TYPE_BINARY,
        PAYLOAD_TYPE_MIME;

        public String getValue()
        {
            switch (this)
            {
                case PAYLOAD_TYPE_TEXT:
                    return "Text";
                case PAYLOAD_TYPE_BINARY:
                    return "Binary Array";
                case PAYLOAD_TYPE_MIME:
                    return "Various MIME Types";
                default:
                    return "";
            }
        }
    }

    private enum PayloadSize
    {
        PAYLOAD_SIZE_SMALL,
        PAYLOAD_SIZE_LARGE;

        public String getValue()
        {
            switch (this)
            {
                case PAYLOAD_SIZE_SMALL:
                    return "Small";
                case PAYLOAD_SIZE_LARGE:
                    return "Large";
                default:
                    return "";
            }
        }
    }

    private enum SecurityType
    {
        SECURITY_TYPE_DTLS,
        SECURITY_TYPE_TLS,
        SECURITY_TYPE_COMPLEX;

        public String getValue()
        {
            switch (this)
            {
                case SECURITY_TYPE_DTLS:
                    return "DTLS";
                case SECURITY_TYPE_TLS:
                    return "TLS";
                case SECURITY_TYPE_COMPLEX:
                    return "Has own complex layer";
                default:
                    return "";
            }
        }
    }

    private enum AuthenticationType
    {
        AUTHENTICATION_TYPE_SIMPLE,
        AUTHENTICATION_TYPE_SASL,
        AUTHENTICATION_TYPE_COMPLEX;

        public String getValue()
        {
            switch (this)
            {
                case AUTHENTICATION_TYPE_SIMPLE:
                    return "Simple (Login/Password)";
                case AUTHENTICATION_TYPE_SASL:
                    return "SASL";
                case AUTHENTICATION_TYPE_COMPLEX:
                    return "Has own complex layer";
                default:
                    return "";
            }
        }
    }

    private enum QoSLevel
    {
        QOS_LEVEL_TCP,
        QOS_LEVEL_THREE,
        QOS_LEVEL_COMPLEX;

        public String getValue()
        {
            switch (this)
            {
                case QOS_LEVEL_TCP:
                    return "TCP";
                case QOS_LEVEL_THREE:
                    return "3-level QOS";
                case QOS_LEVEL_COMPLEX:
                    return "Complex";
                default:
                    return "";
            }
        }
    }

    private enum IoTLevel
    {
        IOT_LEVEL_DD,
        IOT_LEVEL_DG,
        IOT_LEVEL_GS,
        IOT_LEVEL_SS;

        public String getValue()
        {
            switch (this)
            {
                case IOT_LEVEL_DD:
                    return "Device to Device";
                case IOT_LEVEL_DG:
                    return "Device to Gateway";
                case IOT_LEVEL_GS:
                    return "Gateway to Server";
                case IOT_LEVEL_SS:
                    return "Server to Server";
                default:
                    return "";
            }
        }
    }

    //endregion

}
