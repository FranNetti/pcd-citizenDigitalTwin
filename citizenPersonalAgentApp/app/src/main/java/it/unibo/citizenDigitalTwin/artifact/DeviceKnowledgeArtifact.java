package it.unibo.citizenDigitalTwin.artifact;

import android.util.Log;

import org.json.JSONException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import cartago.LINK;
import cartago.OpFeedbackParam;
import it.unibo.citizenDigitalTwin.data.connection.channel.response.ChannelResponse;
import it.unibo.citizenDigitalTwin.data.connection.channel.HttpChannel;
import it.unibo.citizenDigitalTwin.data.connection.channel.MockDeviceKnowledgeChannel;
import it.unibo.citizenDigitalTwin.data.connection.channel.response.DeviceKnowledgeResponse;
import it.unibo.citizenDigitalTwin.data.device.DeviceKnowledge;
import it.unibo.pslab.jaca_android.core.JaCaArtifact;

/**
 * Artifact that enables the communication with the remote Device Knowledge entity.
 */
public class DeviceKnowledgeArtifact extends JaCaArtifact {

    private static final String TAG = "[DeviceKnowledgeArtifact]";
    private static final String SENSOR_RESOURCE = "sensor/";

    private HttpChannel channel;

    void init(){
        channel = new MockDeviceKnowledgeChannel();
    }

    /**
     * Look for the given model's device knowledge.
     * @param model the model of the device you want to know the knowledge of
     * @param knowledge the knowledge found
     */
    @LINK
    public void findDeviceKnowledge(final String model, final OpFeedbackParam<DeviceKnowledgeResponse> knowledge){
        final CompletableFuture<ChannelResponse> future = channel.get(SENSOR_RESOURCE + model);
        try {
            final ChannelResponse response = future.get();
            if(response.getData().isPresent()){
                knowledge.set(
                        DeviceKnowledgeResponse.successfulResponse(new DeviceKnowledge(response.getData().get()))
                );
            } else {
                knowledge.set(DeviceKnowledgeResponse.failedResponse(response.getCode()));
            }
        } catch (final ExecutionException | InterruptedException | JSONException e) {
            Log.e(TAG, "Error in findDeviceKnowledge: " + e.getLocalizedMessage());
            knowledge.set(DeviceKnowledgeResponse.applicationErrorResponse());
        }
    }

}
