package org.chrisblakely.patientservice.service;

import com.google.api.Billing;
import org.chrisblakely.patientservice.dto.PatientRequestDTO;
import org.chrisblakely.patientservice.dto.PatientResponseDTO;
import org.chrisblakely.patientservice.exception.EmailAlreadyExitsException;
import org.chrisblakely.patientservice.exception.PatientNotFoundException;
import org.chrisblakely.patientservice.grpc.BillingServiceGrpcClient;
import org.chrisblakely.patientservice.kafka.KafkaProducer;
import org.chrisblakely.patientservice.mapper.PatientMapper;
import org.chrisblakely.patientservice.model.Patient;
import org.chrisblakely.patientservice.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {


    private  final PatientRepository patientRepository;

    private  final BillingServiceGrpcClient billingServiceGrpcClient;

    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository,
                          BillingServiceGrpcClient billingServiceGrpcClient,
                          KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepository.findAll();

        List<PatientResponseDTO> patientDTOs = patients.stream().map(patient -> PatientMapper.toDTO(patient)).toList();
        return patientDTOs;
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){

        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailAlreadyExitsException("A patient with this email "+ "already exists"+patientRequestDTO.getEmail());
        }

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(), newPatient.getName(),newPatient.getEmail());

        kafkaProducer.sendEvent(newPatient);


        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO){
         Patient patient = patientRepository.findById(id).orElseThrow(
                 ()-> new PatientNotFoundException("Patient not found with ID:"+ id));

        if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),id)){
            throw new EmailAlreadyExitsException("A patient with this email "+ "already exists"+patientRequestDTO.getEmail());
        }

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }


    public void deletePatient(UUID id){
        patientRepository.deleteById(id);
    }
}
