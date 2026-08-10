package com.example.photography.service.impl;

import com.example.photography.dto.request.LeaveApprovalRequest;
import com.example.photography.model.entity.CheckinConfiguration;
import com.example.photography.model.entity.CheckinRecord;
import com.example.photography.model.entity.DutyRecord;
import com.example.photography.model.entity.DutySchedule;
import com.example.photography.model.entity.LeaveRequest;
import com.example.photography.model.entity.User;
import com.example.photography.model.enums.UserRole;
import com.example.photography.repository.CheckinConfigurationRepository;
import com.example.photography.repository.CheckinRecordRepository;
import com.example.photography.repository.DutyRecordRepository;
import com.example.photography.repository.DutyScheduleRepository;
import com.example.photography.repository.LeaveRequestRepository;
import com.example.photography.repository.UserRepository;
import com.example.photography.service.EmailNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeaveServiceImplTest {
    private LeaveRequestRepository leaveRequestRepository;
    private UserRepository userRepository;
    private DutyRecordRepository dutyRecordRepository;
    private CheckinRecordRepository checkinRecordRepository;
    private DutyScheduleRepository dutyScheduleRepository;
    private CheckinConfigurationRepository checkinConfigurationRepository;
    private EmailNotificationService emailNotificationService;
    private LeaveServiceImpl service;

    @BeforeEach
    void setUp() {
        leaveRequestRepository = mock(LeaveRequestRepository.class);
        userRepository = mock(UserRepository.class);
        dutyRecordRepository = mock(DutyRecordRepository.class);
        checkinRecordRepository = mock(CheckinRecordRepository.class);
        dutyScheduleRepository = mock(DutyScheduleRepository.class);
        checkinConfigurationRepository = mock(CheckinConfigurationRepository.class);
        emailNotificationService = mock(EmailNotificationService.class);

        service = new LeaveServiceImpl();
        ReflectionTestUtils.setField(service, "leaveRequestRepository", leaveRequestRepository);
        ReflectionTestUtils.setField(service, "userRepository", userRepository);
        ReflectionTestUtils.setField(service, "dutyRecordRepository", dutyRecordRepository);
        ReflectionTestUtils.setField(service, "checkinRecordRepository", checkinRecordRepository);
        ReflectionTestUtils.setField(service, "dutyScheduleRepository", dutyScheduleRepository);
        ReflectionTestUtils.setField(service, "checkinConfigurationRepository", checkinConfigurationRepository);
        ReflectionTestUtils.setField(service, "emailNotificationService", emailNotificationService);
    }

    @Test
    void approveCheckinLeaveOnlyCreatesRecordForSelectedConfiguration() {
        LocalDate leaveDate = LocalDate.of(2026, 5, 7);
        User user = user(1L, "member");
        User approver = user(2L, "admin");
        approver.setRole(UserRole.SUPER_ADMIN);
        CheckinConfiguration selectedConfig = checkinConfig(10L, "晚自习一", LocalTime.of(19, 0));

        LeaveRequest leaveRequest = leaveRequest(user, LeaveRequest.LeaveType.CHECKIN_LEAVE, leaveDate);
        leaveRequest.setCheckinConfigurationId(selectedConfig.getId());

        when(leaveRequestRepository.findByIdWithUser(100L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByIdWithDepartment(approver.getId())).thenReturn(Optional.of(approver));
        when(checkinConfigurationRepository.findById(selectedConfig.getId())).thenReturn(Optional.of(selectedConfig));
        when(checkinRecordRepository.findByUserAndConfigurationAndDate(user, selectedConfig, leaveDate))
                .thenReturn(Optional.empty());

        service.approveLeaveRequest(100L, approval(), approver.getId());

        ArgumentCaptor<CheckinRecord> captor = ArgumentCaptor.forClass(CheckinRecord.class);
        verify(checkinRecordRepository).save(captor.capture());
        CheckinRecord savedRecord = captor.getValue();

        assertThat(savedRecord.getConfiguration()).isSameAs(selectedConfig);
        assertThat(savedRecord.getStatus()).isEqualTo(CheckinRecord.CheckinStatus.LEAVE);
        verify(emailNotificationService).notifyLeaveApprovedToApplicant(leaveRequest);
        verify(checkinConfigurationRepository, never()).findByIsActiveTrueAndDeletedFalseOrderBySortOrderAsc();
    }

    @Test
    void approveDutyLeaveOnlyCreatesRecordForSelectedSchedule() {
        LocalDate leaveDate = LocalDate.of(2026, 5, 9);
        User user = user(1L, "member");
        User approver = user(2L, "admin");
        approver.setRole(UserRole.SUPER_ADMIN);
        DutySchedule selectedSchedule = dutySchedule(20L, user, leaveDate.getDayOfWeek().getValue());

        LeaveRequest leaveRequest = leaveRequest(user, LeaveRequest.LeaveType.DUTY_LEAVE, leaveDate);
        leaveRequest.setDutyScheduleIds(selectedSchedule.getId().toString());

        when(leaveRequestRepository.findByIdWithUser(100L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByIdWithDepartment(approver.getId())).thenReturn(Optional.of(approver));
        when(dutyScheduleRepository.findById(selectedSchedule.getId())).thenReturn(Optional.of(selectedSchedule));
        when(dutyRecordRepository.findByUser_IdAndDutyDateAndDutySchedule_IdAndDeletedFalse(
                user.getId(), leaveDate, selectedSchedule.getId())).thenReturn(Optional.empty());

        service.approveLeaveRequest(100L, approval(), approver.getId());

        ArgumentCaptor<DutyRecord> captor = ArgumentCaptor.forClass(DutyRecord.class);
        verify(dutyRecordRepository).save(captor.capture());
        DutyRecord savedRecord = captor.getValue();

        assertThat(savedRecord.getDutySchedule()).isSameAs(selectedSchedule);
        assertThat(savedRecord.getStatus()).isEqualTo("已请假");
        verify(emailNotificationService).notifyLeaveApprovedToApplicant(leaveRequest);
        verify(dutyScheduleRepository, never())
                .findByUser_IdAndDayOfWeekAndActiveTrueAndDeletedFalse(eq(user.getId()), any(Integer.class));
    }

    @Test
    void rejectedLeaveDoesNotNotifyApplicantAsApproved() {
        LocalDate leaveDate = LocalDate.of(2026, 5, 9);
        User user = user(1L, "member");
        User approver = user(2L, "admin");
        approver.setRole(UserRole.SUPER_ADMIN);
        LeaveRequest leaveRequest = leaveRequest(user, LeaveRequest.LeaveType.OTHER, leaveDate);

        LeaveApprovalRequest rejection = new LeaveApprovalRequest();
        rejection.setStatus(LeaveRequest.RequestStatus.REJECTED);
        rejection.setApproveNotes("不同意");

        when(leaveRequestRepository.findByIdWithUser(100L)).thenReturn(Optional.of(leaveRequest));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByIdWithDepartment(approver.getId())).thenReturn(Optional.of(approver));

        service.approveLeaveRequest(100L, rejection, approver.getId());

        verify(emailNotificationService, never()).notifyLeaveApprovedToApplicant(any(LeaveRequest.class));
    }

    @Test
    void checkinLeaveWithoutConfigurationDoesNotApplyToSpecificConfiguration() {
        LocalDate leaveDate = LocalDate.of(2026, 5, 9);
        User user = user(1L, "member");
        LeaveRequest legacyLeave = leaveRequest(user, LeaveRequest.LeaveType.CHECKIN_LEAVE, leaveDate);
        legacyLeave.setCheckinConfigurationId(null);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(leaveRequestRepository.findApprovedLeaveForUserAndDate(user, leaveDate))
                .thenReturn(Collections.singletonList(legacyLeave));

        assertThat(service.hasApprovedCheckinLeave(user.getId(), leaveDate, 10L)).isFalse();
    }

    private LeaveApprovalRequest approval() {
        LeaveApprovalRequest request = new LeaveApprovalRequest();
        request.setStatus(LeaveRequest.RequestStatus.APPROVED);
        request.setApproveNotes("同意");
        return request;
    }

    private LeaveRequest leaveRequest(User user, LeaveRequest.LeaveType leaveType, LocalDate leaveDate) {
        LeaveRequest request = new LeaveRequest();
        request.setId(100L);
        request.setUser(user);
        request.setLeaveType(leaveType);
        request.setStartDate(leaveDate);
        request.setEndDate(leaveDate);
        request.setReason("请假");
        request.setStatus(LeaveRequest.RequestStatus.PENDING);
        return request;
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("password");
        user.setRealName(username);
        user.setRole(UserRole.MEMBER);
        return user;
    }

    private CheckinConfiguration checkinConfig(Long id, String name, LocalTime startTime) {
        CheckinConfiguration config = new CheckinConfiguration();
        config.setId(id);
        config.setName(name);
        config.setLocationName("教室");
        config.setSessionName(name);
        config.setStartTime(startTime);
        config.setEndTime(startTime.plusHours(1));
        config.setIsActive(true);
        return config;
    }

    private DutySchedule dutySchedule(Long id, User user, Integer dayOfWeek) {
        DutySchedule schedule = new DutySchedule();
        schedule.setId(id);
        schedule.setUser(user);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(10, 0));
        schedule.setActive(true);
        return schedule;
    }
}
