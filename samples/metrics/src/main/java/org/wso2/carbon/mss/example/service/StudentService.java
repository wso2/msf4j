/*
 * Copyright 2015 WSO2 Inc. (http://wso2.org)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.mss.example.service;

import org.wso2.carbon.metrics.annotation.Counted;
import org.wso2.carbon.metrics.annotation.Metered;
import org.wso2.carbon.metrics.annotation.Timed;
import org.wso2.carbon.mss.httpmonitoring.HTTPMonitoring;
import org.wso2.carbon.mss.httpmonitoring.HTTPMonitoringModule;
import org.wso2.carbon.mss.metrics.MetricReporter;
import org.wso2.carbon.mss.metrics.MetricsModule;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * StudentService class.
 */
@Path("/student")
public class StudentService {

    private Map<String, Student> students = new ConcurrentHashMap<>();

    public StudentService() {
        Student student = new Student();
        student.setNic("910760234V");
        student.setFirstName("Joseph");
        student.setLastName("Rodgers");
        student.setAge(14);
        addStudent(student);
    }

    @PostConstruct
    public void init() {
        MetricsModule.init(MetricReporter.CONSOLE, MetricReporter.JMX, MetricReporter.DAS);
        HTTPMonitoringModule.init();
    }

    @PreDestroy
    public void destroy() {
        MetricsModule.destroy();
        HTTPMonitoringModule.destroy();
    }

    @GET
    @Path("/{nic}")
    @Produces("application/json")
    @Timed
    @HTTPMonitoring
    public Student getStudent(@PathParam("nic") String nic) {
        return students.get(nic);
    }

    @POST
    @Consumes("application/json")
    @Metered
    @HTTPMonitoring
    public void addStudent(Student student) {
        students.put(student.getNic(), student);
    }

    @GET
    @Produces("application/json")
    @Counted
    @HTTPMonitoring
    public Collection<Student> getAll() {
        return students.values();
    }

}
