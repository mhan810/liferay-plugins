/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.calendar.service.impl;

import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.notification.NotificationType;
import com.liferay.calendar.recurrence.Frequency;
import com.liferay.calendar.recurrence.Recurrence;
import com.liferay.calendar.recurrence.RecurrenceSerializer;
import com.liferay.calendar.recurrence.Weekday;
import com.liferay.calendar.service.base.CalendarImporterLocalServiceBaseImpl;
import com.liferay.calendar.util.CalendarResourceUtil;
import com.liferay.portal.kernel.cal.DayAndPosition;
import com.liferay.portal.kernel.cal.TZSRecurrence;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.asset.model.AssetEntry;
import com.liferay.portlet.asset.model.AssetLink;
import com.liferay.portlet.calendar.model.CalEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcellus Tavares
 */
public class CalendarImporterLocalServiceImpl
	extends CalendarImporterLocalServiceBaseImpl {

	public void importCalEvent(CalEvent calEvent)
		throws PortalException, SystemException {

		if (calEvent.isImported()) {
			return;
		}

		long companyId = calEvent.getCompanyId();

		CalendarResource calendarResource = getCalendarResource(
			companyId, calEvent.getGroupId());

		Date startDate = calEvent.getStartDate();

		long startTime = startDate.getTime();

		long endTime =
			startTime + calEvent.getDurationHour() * Time.HOUR +
			calEvent.getDurationMinute() * Time.MINUTE;

		if (calEvent.isAllDay()) {
			endTime = endTime - 1;
		}

		String recurrence = getRecurrence(calEvent.getRecurrenceObj());

		CalendarBooking calendarBooking = addCalendarBooking(
			calEvent.getUuid(), companyId, calendarResource.getGroupId(),
			calEvent.getUserId(), calEvent.getUserName(),
			calEvent.getCreateDate(), calEvent.getModifiedDate(),
			calendarResource.getDefaultCalendarId(),
			calendarResource.getCalendarResourceId(), calEvent.getTitle(),
			calEvent.getDescription(), calEvent.getLocation(), startTime,
			endTime, calEvent.getAllDay(), recurrence,
			calEvent.getFirstReminder(), NotificationType.EMAIL,
			calEvent.getSecondReminder(), NotificationType.EMAIL);

		// Asset

		AssetEntry assetEntry = assetEntryPersistence.fetchByC_C(
			PortalUtil.getClassNameId(CalEvent.class.getName()),
			calEvent.getEventId());

		importAssetEntry(assetEntry, calendarBooking);

		// Cal Event

		calEvent.setImported(true);

		calEventPersistence.update(calEvent);
	}

	public void importCalEvents() throws PortalException, SystemException {
		List<CalEvent> calEvents = calEventLocalService.getNotImportedEvents();

		for (CalEvent calEvent : calEvents) {
			importCalEvent(calEvent);
		}
	}

	protected CalendarBooking addCalendarBooking(
			String uuid, long companyId, long groupId, long userId,
			String userName, Date createDate, Date modifiedDate,
			long calendarId, long calendarResourceId, String title,
			String description, String location, long startTime, long endTime,
			boolean allDay, String recurrence, int firstReminder,
			NotificationType firstReminderType, int secondReminder,
			NotificationType secondReminderType)
		throws PortalException, SystemException {

		long calendarBookingId = counterLocalService.increment();

		CalendarBooking calendarBooking = calendarBookingPersistence.create(
			calendarBookingId);

		calendarBooking.setUuid(uuid);
		calendarBooking.setCalendarBookingId(calendarBookingId);
		calendarBooking.setCompanyId(companyId);
		calendarBooking.setGroupId(groupId);
		calendarBooking.setUserId(userId);
		calendarBooking.setUserName(userName);
		calendarBooking.setCreateDate(createDate);
		calendarBooking.setModifiedDate(modifiedDate);
		calendarBooking.setCalendarId(calendarId);
		calendarBooking.setCalendarResourceId(calendarResourceId);
		calendarBooking.setParentCalendarBookingId(calendarBookingId);
		calendarBooking.setTitle(title);
		calendarBooking.setDescription(description);
		calendarBooking.setLocation(location);
		calendarBooking.setStartTime(startTime);
		calendarBooking.setEndTime(endTime);
		calendarBooking.setAllDay(allDay);
		calendarBooking.setRecurrence(recurrence);
		calendarBooking.setFirstReminder(firstReminder);
		calendarBooking.setFirstReminderType(firstReminderType.toString());
		calendarBooking.setSecondReminder(firstReminder);
		calendarBooking.setSecondReminderType(secondReminderType.toString());
		calendarBooking.setStatus(WorkflowConstants.STATUS_APPROVED);
		calendarBooking.setStatusByUserId(userId);
		calendarBooking.setStatusByUserName(userName);
		calendarBooking.setStatusDate(createDate);

		return calendarBookingPersistence.update(calendarBooking);
	}

	protected CalendarResource getCalendarResource(long companyId, long groupId)
		throws PortalException, SystemException {

		long userId = UserLocalServiceUtil.getDefaultUserId(companyId);

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(companyId);
		serviceContext.setUserId(userId);

		CalendarResource calendarResource =
			CalendarResourceUtil.getGroupCalendarResource(
				groupId, serviceContext);

		return calendarResource;
	}

	protected String getRecurrence(TZSRecurrence tzsRecurrence) {
		if (tzsRecurrence == null) {
			return null;
		}

		Recurrence recurrence = new Recurrence();

		Frequency frequency = _frequencyMap.get(tzsRecurrence.getFrequency());

		int interval = tzsRecurrence.getInterval();

		List<Weekday> weekdays = new ArrayList<Weekday>();

		if ((frequency == Frequency.DAILY) && (interval == 0)) {
			frequency = Frequency.WEEKLY;

			interval = 1;

			weekdays.add(Weekday.MONDAY);
			weekdays.add(Weekday.TUESDAY);
			weekdays.add(Weekday.WEDNESDAY);
			weekdays.add(Weekday.THURSDAY);
			weekdays.add(Weekday.FRIDAY);

			recurrence.setWeekdays(weekdays);
		}
		else if (frequency == Frequency.WEEKLY) {
			DayAndPosition[] dayAndPositions = tzsRecurrence.getByDay();

			for (DayAndPosition dayAndPosition : dayAndPositions) {
				Weekday weekday = _weekdayMap.get(
					dayAndPosition.getDayOfWeek());

				weekdays.add(weekday);
			}
		}

		recurrence.setInterval(interval);
		recurrence.setFrequency(frequency);
		recurrence.setWeekdays(weekdays);

		Calendar untilJCalendar = tzsRecurrence.getUntil();

		int ocurrence = tzsRecurrence.getOccurrence();

		if (untilJCalendar != null) {
			recurrence.setUntilJCalendar(untilJCalendar);
		}
		else if (ocurrence > 0) {
			recurrence.setCount(ocurrence);
		}

		return RecurrenceSerializer.serialize(recurrence);
	}

	protected void importAssetEntry(
			AssetEntry assetEntry, CalendarBooking calendarBooking)
		throws PortalException, SystemException {

		if (assetEntry == null) {
			return;
		}

		List<AssetLink> assetLinks = assetLinkLocalService.getDirectLinks(
			assetEntry.getEntryId());

		long[] assetLinkEntryIds = StringUtil.split(
			ListUtil.toString(assetLinks, "entryId2"), 0L);

		calendarBookingLocalService.updateAsset(
			assetEntry.getUserId(), calendarBooking,
			assetEntry.getCategoryIds(), assetEntry.getTagNames(),
			assetLinkEntryIds);
	}

	private static Map<Integer, Frequency> _frequencyMap =
		new HashMap<Integer, Frequency>();
	private static Map<Integer, Weekday> _weekdayMap =
		new HashMap<Integer, Weekday>();

	static {
		_frequencyMap.put(TZSRecurrence.DAILY, Frequency.DAILY);
		_frequencyMap.put(TZSRecurrence.WEEKLY, Frequency.WEEKLY);
		_frequencyMap.put(TZSRecurrence.MONTHLY, Frequency.MONTHLY);
		_frequencyMap.put(TZSRecurrence.YEARLY, Frequency.YEARLY);

		_weekdayMap.put(Calendar.SUNDAY, Weekday.SUNDAY);
		_weekdayMap.put(Calendar.MONDAY, Weekday.MONDAY);
		_weekdayMap.put(Calendar.TUESDAY, Weekday.TUESDAY);
		_weekdayMap.put(Calendar.WEDNESDAY, Weekday.WEDNESDAY);
		_weekdayMap.put(Calendar.THURSDAY, Weekday.THURSDAY);
		_weekdayMap.put(Calendar.FRIDAY, Weekday.FRIDAY);
		_weekdayMap.put(Calendar.SATURDAY, Weekday.SATURDAY);
	}

}