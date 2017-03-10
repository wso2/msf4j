/*
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.msf4j.sample.websocket.echoserver.httpmonitoring.service;


import org.wso2.carbon.metrics.core.annotation.Timed;
import org.wso2.msf4j.analytics.httpmonitoring.HTTPMonitored;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
@Timed
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

    @GET
    @Path("/{nic}")
    @Produces("application/json")
    @HTTPMonitored(tracing = true)
    public Student getStudent(@PathParam("nic") String nic) {
        return students.get(nic);
    }

    @POST
    @Consumes("application/json")
    @HTTPMonitored(tracing = true)
    public void addStudent(Student student) {
        students.put(student.getNic(), student);
    }

    @GET
    @Produces("application/json")
    @HTTPMonitored
    public Collection<Student> getAll() {
        return students.values();
    }

}
