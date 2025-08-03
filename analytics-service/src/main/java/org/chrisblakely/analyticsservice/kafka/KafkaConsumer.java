package org.chrisblakely.analyticsservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.kafka.annotation.KafkaListener;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(
            KafkaConsumer.class);
//
//    @KafkaListener(topics="patient", groupId = "analytics-service")
//    public void consumeEvent(byte[] event) {
//        try {
//            PatientEvent patientEvent = PatientEvent.parseFrom(event);
//            // ... perform any business related to analytics here
//
//            log.info("Received Patient Event: [PatientId={},PatientName={},PatientEmail={}]",
//                    patientEvent.getPatientId(),
//                    patientEvent.getName(),
//                    patientEvent.getEmail());
//        } catch (InvalidProtocolBufferException e) {
//            log.error("Error deserializing event {}", e.getMessage());
//        }
//    }

    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consumeEvent(ConsumerRecord<String, byte[]> record){
        byte[] event = record.value();

        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);
            log.info("Received patient event: [PatientId={}, PatientName={}, PatientEmail={}]",
                    patientEvent.getPatientId(), patientEvent.getName(), patientEvent.getEmail());
        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing event: {}", e.getMessage());
        }
    }
}