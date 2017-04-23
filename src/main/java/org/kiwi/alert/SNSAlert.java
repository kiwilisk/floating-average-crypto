package org.kiwi.alert;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.math.BigDecimal;
import org.kiwi.config.EnvironmentVariables;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class SNSAlert implements DeviationAlert {

    private final AmazonSNS snsClient;
    private final String topic;

    @Inject
    SNSAlert(AmazonSNS snsClient, @Named("sns.alert.topic.arn.env.variable") String topicEnvVariable,
            EnvironmentVariables environmentVariables) {
        this.snsClient = snsClient;
        this.topic = environmentVariables.getValueFrom(topicEnvVariable)
                .orElseThrow(() -> new RuntimeException(
                        "Failed to load SNS topic from environment with key [" + topicEnvVariable + "]"));
    }

    @Override
    public void alert(FloatingAverage floatingAverage) {
        PublishRequest publishRequest = createPublishRequestWith(floatingAverage);
        snsClient.publish(publishRequest);
    }

    private PublishRequest createPublishRequestWith(FloatingAverage floatingAverage) {
        String subject = "Floating average warning";
        String messageBody = createMessageBodyWith(floatingAverage);
        return new PublishRequest()
                .withTopicArn(topic)
                .withSubject(subject)
                .withMessage(messageBody);
    }

    private String createMessageBodyWith(FloatingAverage floatingAverage) {
        String recommendation = isValueHigherThanAverage(floatingAverage) ? "buy" : "sell";
        return "Recommendation: " + recommendation + "!\n" +
                "The current closing value " + floatingAverage.getLatestQuoteValue() +
                " of observed asset " + floatingAverage.getName() +
                " deviates more than " + floatingAverage.getDeviationThreshold() +
                " percent from the current floating average of " + floatingAverage.getLatestAverage() + ".";
    }

    private boolean isValueHigherThanAverage(FloatingAverage floatingAverage) {
        BigDecimal latestQuoteValue = new BigDecimal(floatingAverage.getLatestQuoteValue());
        BigDecimal latestAverage = new BigDecimal(floatingAverage.getLatestAverage());
        return latestQuoteValue.compareTo(latestAverage) == 1;
    }
}
