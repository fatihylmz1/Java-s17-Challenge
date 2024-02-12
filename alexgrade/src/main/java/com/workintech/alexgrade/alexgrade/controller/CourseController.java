package com.workintech.alexgrade.alexgrade.controller;

import com.workintech.alexgrade.alexgrade.exceptions.DuplicateCourseException;
import com.workintech.alexgrade.alexgrade.exceptions.InvalidCreditValueException;
import com.workintech.alexgrade.alexgrade.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/workintech/courses")
public class CourseController {
    private List<Course> courses = new ArrayList<>();
    private LowCourseGpa lowCourseGpa;
    private MediumCourseGpa mediumCourseGpa;
    private HighCourseGpa highCourseGpa;

    @Autowired
    public CourseController(LowCourseGpa lowCourseGpa, MediumCourseGpa mediumCourseGpa, HighCourseGpa highCourseGpa) {
        this.lowCourseGpa = lowCourseGpa;
        this.mediumCourseGpa = mediumCourseGpa;
        this.highCourseGpa = highCourseGpa;
    }
    private void validateCredit(int credit) {
        if (credit <= 0 || credit > 4) {
            throw new InvalidCreditValueException("Credit value must be between 1 and 4 (inclusive).", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        try {
            return new ResponseEntity<>(courses, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{name}")
    public ResponseEntity<Course> getCourseByName(@PathVariable String name) {
        try {
            for (Course course : courses) {
                if (course.getName().equals(name)) {
                    return new ResponseEntity<>(course, HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addCourse(@RequestBody Course course) {
        try {
            validateCredit(course.getCredit());
            courses.add(course);
            return new ResponseEntity<>("Course added successfully", HttpStatus.OK);
        } catch (InvalidCreditValueException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DuplicateCourseException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception ex) {
            return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private double calculateTotalGpa(Course course) {
        double coefficient = course.getGrade().getCoefficient();
        int credit = course.getCredit();

        if (credit <= 2) {
            return coefficient * credit * lowCourseGpa.getGpa();
        } else if (credit == 3) {
            return coefficient * credit * mediumCourseGpa.getGpa();
        } else {
            return coefficient * credit * highCourseGpa.getGpa();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourseById(@PathVariable int id) {
        try {
            Course courseToRemove = null;
            for (Course course : courses) {
                if (course.getId() == id) {
                    courseToRemove = course;
                    break;
                }
            }

            if (courseToRemove != null) {
                courses.remove(courseToRemove);
                return new ResponseEntity<>("Course with ID " + id + " has been deleted.", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Course with ID " + id + " not found.", HttpStatus.NOT_FOUND);
            }
        } catch (Exception ex) {
            // Handle other exceptions
            return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
