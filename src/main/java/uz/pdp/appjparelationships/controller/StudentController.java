package uz.pdp.appjparelationships.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import uz.pdp.appjparelationships.entity.Address;
import uz.pdp.appjparelationships.entity.Group;
import uz.pdp.appjparelationships.entity.Student;
import uz.pdp.appjparelationships.entity.Subject;
import uz.pdp.appjparelationships.payload.StudentDto;
import uz.pdp.appjparelationships.repository.AddressRepository;
import uz.pdp.appjparelationships.repository.GroupRepository;
import uz.pdp.appjparelationships.repository.StudentRepository;
import uz.pdp.appjparelationships.repository.SubjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/student")
public class StudentController {

    private final StudentRepository studentRepository;
    private final AddressRepository addressRepository;
    private final GroupRepository groupRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public StudentController(StudentRepository studentRepository, AddressRepository addressRepository, GroupRepository groupRepository, SubjectRepository subjectRepository) {
        this.studentRepository = studentRepository;
        this.addressRepository = addressRepository;
        this.groupRepository = groupRepository;
        this.subjectRepository = subjectRepository;
    }

    //1. VAZIRLIK
    @GetMapping("/forMinistry")
    public Page<Student> getStudentListForMinistry(@RequestParam int page) {
        //1-1=0     2-1=1    3-1=2    4-1=3
        //select * from student limit 10 offset (0*10)
        //select * from student limit 10 offset (1*10)
        //select * from student limit 10 offset (2*10)
        //select * from student limit 10 offset (3*10)
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAll(pageable);
    }

    //2. UNIVERSITY
    @GetMapping("/forUniversity/{universityId}")
    public Page<Student> getStudentListForUniversity(@PathVariable Integer universityId,
                                                     @RequestParam int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAllByGroup_Faculty_UniversityId(universityId, pageable);
    }

    //3. FACULTY DEKANAT
    @GetMapping("/forFaculty/{facultyId}")
    public Page<Student> getStudentListForFaculty(@PathVariable Integer facultyId,
                                                  @RequestParam int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAllByGroup_FacultyId(facultyId, pageable);
    }

    //4. GROUP OWNER
    @GetMapping("/forGroup/{groupId}")
    public Page<Student> getStudentListForGroup(@PathVariable Integer groupId,
                                                @RequestParam int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return studentRepository.findAllByGroupId(groupId, pageable);
    }

    @GetMapping("/getStudent/{studentId}")
    public Student getStudentListForStudent(@PathVariable Integer studentId){
        Optional<Student> optionalStudent = studentRepository.findById(studentId);
        if (!optionalStudent.isPresent()){
            throw new RuntimeException("Student not found");
        }
        return optionalStudent.get();
    }

    @PostMapping("/createStudent")
    public Student addStudent(@RequestBody StudentDto dto) {
        boolean exists = studentRepository.existsByFirstNameAndLastNameAndGroupId(dto.getFirstName(), dto.getLastName(), dto.getGroupId());
        if (exists){
            throw new RuntimeException("This student is already exist in this group");
        }
        Optional<Address> optionalAddress = addressRepository.findById(dto.getAddressId());
        if (!optionalAddress.isPresent()) {
            throw new RuntimeException("Address is not found");
        }
        Optional<Group> optionalGroup = groupRepository.findById(dto.getGroupId());
        if (!optionalGroup.isPresent()) {
            throw new RuntimeException("Group is not found");
        }
        List<Subject> subjects = new ArrayList<>();
        List<Integer> subjectId = dto.getSubjectId();
        for (Integer subjectById : subjectId) {
            Optional<Subject> optionalSubject = subjectRepository.findById(subjectById);
            if (!optionalSubject.isPresent()) {
                throw new RuntimeException("Subject is not found");
            }
            subjects.add(optionalSubject.get());
        }

        Student student = Student.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .address(optionalAddress.get())
                .group(optionalGroup.get())
                .subjects(subjects).build();
        return studentRepository.save(student);
    }

    @PutMapping("/update/{id}")
    public Student update(@PathVariable Integer id, @RequestBody StudentDto dto){
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (!optionalStudent.isPresent()){
            throw new RuntimeException("Student not found");
        }

        boolean exists = studentRepository.existsByFirstNameAndLastNameAndGroupId(dto.getFirstName(), dto.getLastName(), dto.getGroupId());
        if (exists) {
            throw new RuntimeException("This student is already exist in this group");
        }

        Optional<Address> optionalAddress = addressRepository.findById(dto.getAddressId());
        if (!optionalAddress.isPresent()) {
            throw new RuntimeException("Address is not found");
        }

        Optional<Group> optionalGroup = groupRepository.findById(dto.getGroupId());
        if (!optionalGroup.isPresent()) {
            throw new RuntimeException("Group is not found");
        }

        List<Subject> subjects = new ArrayList<>();
        List<Integer> subjectId = dto.getSubjectId();
        for (Integer subjectById : subjectId) {
            Optional<Subject> optionalSubject = subjectRepository.findById(subjectById);
            if (!optionalSubject.isPresent()) {
                throw new RuntimeException("Subject is not found");
            }
            subjects.add(optionalSubject.get());
        }
        Student student = optionalStudent.get();
        student = Student.builder()
                .firstName(dto.getFirstName() != null ? dto.getFirstName() : student.getFirstName())
                .lastName(dto.getLastName() != null ? dto.getLastName() : student.getLastName())
                .address(optionalAddress.get())
                .subjects(subjects)
                .group(optionalGroup.get()).build();
        return studentRepository.save(student);
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        try {
            studentRepository.deleteById(id);
            return "Student deleted";
        } catch (Exception e) {
            return "Error in deleting";
        }
    }
}
