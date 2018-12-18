/*
 * Copyright 2018 RAYANOVA.COM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rayanova.solarhijri;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoPeriod;
import java.time.temporal.ChronoField;
import static java.time.temporal.ChronoField.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;

/**
 * A date in the Solar Hijri calendar system.
 */
public class SolarHijriDate implements ChronoLocalDate
{
    /**
     * Days since ISO epoch.
     */
    private static final long EPOCH = -492268;

    /**
     * The proleptic year.
     */
    private final int year;
    /**
     * The month-of-year.
     */
    private final short monthOfYear;
    /**
     * The day-of-month.
     */
    private final short dayOfMonth;

    private SolarHijriDate(int prolepticYear, int monthOfYear, int dayOfMonth)
    {
        this.year = prolepticYear;
        this.monthOfYear = (short) monthOfYear;
        this.dayOfMonth = (short) dayOfMonth;
    }

    public static SolarHijriDate of(int prolepticYear, int monthOfYear, int dayOfMonth)
    {
        YEAR.checkValidValue(prolepticYear);
        MONTH_OF_YEAR.checkValidValue(monthOfYear);
        DAY_OF_MONTH.checkValidValue(dayOfMonth);

        if (dayOfMonth > 29 && dayOfMonth > lengthOfMonth(prolepticYear, monthOfYear))
            throw new DateTimeException(String.format("Invalid date: %s/%s/%s",
                    prolepticYear, monthOfYear, dayOfMonth));

        return new SolarHijriDate(prolepticYear, monthOfYear, dayOfMonth);
    }

    @Override
    public long toEpochDay()
    {
        long firstDayOfYear = 365L * (year - 1L) + Math.floorDiv(8L * year + 21, 33L);
        return EPOCH + firstDayOfYear + getDayOfYear() - 1;
    }

    public static SolarHijriDate ofEpochDay(long epochDay)
    {
        long daysSinceEpoch = epochDay - EPOCH;
        int year = 1 + (int) Math.floorDiv(33 * daysSinceEpoch + 3, 12053);
        long firstDayOfYear = 365L * (year - 1L) + Math.floorDiv(8L * year + 21, 33L);
        int dayOfYear = (int) (daysSinceEpoch - firstDayOfYear) + 1;

        return ofYearDay(year, dayOfYear);
    }

    public static SolarHijriDate ofYearDay(int prolepticYear, int dayOfYear)
    {
        int doy0 = dayOfYear - 1, moy0, dom0;
        if (doy0 < 186) {
            moy0 = doy0 / 31;
            dom0 = doy0 - (moy0 * 31);
        } else {
            moy0 = (doy0 - 186) / 30 + 6;
            dom0 = doy0 - 186 - (moy0 - 6) * 30;
        }
        return new SolarHijriDate(prolepticYear, moy0 + 1, dom0 + 1);
    }

    public static SolarHijriDate from(TemporalAccessor temporal)
    {
        if (temporal instanceof SolarHijriDate)
            return (SolarHijriDate) temporal;
        return SolarHijriDate.ofEpochDay(temporal.getLong(EPOCH_DAY));
    }

    @Override
    public SolarHijriChronology getChronology()
    {
        return SolarHijriChronology.INSTANCE;
    }

    /**
     * @return the proleptic year
     */
    public int getYear()
    {
        return year;
    }

    public int getMonthOfYear()
    {
        return monthOfYear;
    }

    public int getDayOfMonth()
    {
        return dayOfMonth;
    }

    private int getDayOfWeek()
    {
        int dow0 = (int) Math.floorMod(toEpochDay() + 3, 7);
        return dow0 + 1;
    }

    private long getProlepticMonth()
    {
        return (year * 12L + monthOfYear - 1);
    }

    private int getDayOfYear()
    {
        if (monthOfYear < 7)
            return (monthOfYear - 1) * 31 + dayOfMonth;
        return 186 + (monthOfYear - 7) * 30 + dayOfMonth;
    }

    @Override
    public int lengthOfMonth()
    {
        return lengthOfMonth(year, monthOfYear);
    }

    private static int lengthOfMonth(long prolepticYear, int monthOfYear)
    {
        if (monthOfYear == 12)
            return SolarHijriChronology.INSTANCE.isLeapYear(prolepticYear) ? 30 : 29;
        if (monthOfYear > 6)
            return 30;
        return 31;
    }

    private static SolarHijriDate resolvePreviousValid(int year, int month, int day)
    {
        int monthDays = lengthOfMonth(year, month);
        if (day > monthDays)
            day = monthDays;
        return new SolarHijriDate(year, month, day);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ChronoLocalDateTime<SolarHijriDate> atTime(LocalTime localTime)
    {
        return (ChronoLocalDateTime<SolarHijriDate>) ChronoLocalDate.super.atTime(localTime);
    }

    @Override
    public SolarHijriDate with(TemporalAdjuster adjuster)
    {
        return (SolarHijriDate) ChronoLocalDate.super.with(adjuster);
    }

    @Override
    public SolarHijriDate with(TemporalField field, long newValue)
    {
        if (field instanceof ChronoField) {
            ChronoField f = (ChronoField) field;
            f.checkValidValue(newValue);
            switch (f) {
                case DAY_OF_WEEK:
                    return plusDays(newValue - getDayOfWeek());
                case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                    return plusDays(newValue - getLong(ALIGNED_DAY_OF_WEEK_IN_MONTH));
                case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                    return plusDays(newValue - getLong(ALIGNED_DAY_OF_WEEK_IN_YEAR));
                case DAY_OF_MONTH:
                    return withDayOfMonth((int) newValue);
                case DAY_OF_YEAR:
                    return withDayOfYear((int) newValue);
                case EPOCH_DAY:
                    return SolarHijriDate.ofEpochDay(newValue);
                case ALIGNED_WEEK_OF_MONTH:
                    return plusWeeks(newValue - getLong(ALIGNED_WEEK_OF_MONTH));
                case ALIGNED_WEEK_OF_YEAR:
                    return plusWeeks(newValue - getLong(ALIGNED_WEEK_OF_YEAR));
                case MONTH_OF_YEAR:
                    return withMonthOfYear((int) newValue);
                case PROLEPTIC_MONTH:
                    return plusMonths(newValue - getProlepticMonth());
                case YEAR_OF_ERA:
                    return withYear((int) (year >= 1 ? newValue : 1 - newValue));
                case YEAR:
                    return withYear((int) newValue);
                case ERA:
                    return (getLong(ERA) == newValue ? this : withYear(1 - year));
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return (SolarHijriDate) ChronoLocalDate.super.with(field, newValue);
    }

    public SolarHijriDate withYear(int year)
    {
        if (this.year == year)
            return this;
        YEAR.checkValidValue(year);
        return resolvePreviousValid(year, monthOfYear, dayOfMonth);
    }

    public SolarHijriDate withMonthOfYear(int monthOfYear)
    {
        if (this.monthOfYear == monthOfYear)
            return this;
        MONTH_OF_YEAR.checkValidValue(monthOfYear);
        return resolvePreviousValid(year, monthOfYear, dayOfMonth);
    }

    public SolarHijriDate withDayOfMonth(int dayOfMonth)
    {
        if (this.dayOfMonth == dayOfMonth)
            return this;
        return of(year, monthOfYear, dayOfMonth);
    }

    public SolarHijriDate withDayOfYear(int dayOfYear)
    {
        if (this.getDayOfYear() == dayOfYear)
            return this;
        return ofYearDay(year, dayOfYear);
    }

    @Override
    public SolarHijriDate plus(TemporalAmount amount)
    {
        return (SolarHijriDate) ChronoLocalDate.super.plus(amount);
    }

    @Override
    public SolarHijriDate plus(long amountToAdd, TemporalUnit unit)
    {
        if (unit instanceof ChronoUnit) {
            switch ((ChronoUnit) unit) {
                case DAYS:
                    return plusDays(amountToAdd);
                case WEEKS:
                    return plusWeeks(amountToAdd);
                case MONTHS:
                    return plusMonths(amountToAdd);
                case YEARS:
                    return plusYears(amountToAdd);
                case DECADES:
                    return plusYears(Math.multiplyExact(amountToAdd, 10));
                case CENTURIES:
                    return plusYears(Math.multiplyExact(amountToAdd, 100));
                case MILLENNIA:
                    return plusYears(Math.multiplyExact(amountToAdd, 1000));
                case ERAS:
                    return with(ERA, Math.addExact(getLong(ERA), amountToAdd));
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return (SolarHijriDate) ChronoLocalDate.super.plus(amountToAdd, unit);
    }

    public SolarHijriDate plusYears(long yearsToAdd)
    {
        if (yearsToAdd == 0)
            return this;
        int newYear = YEAR.checkValidIntValue(year + yearsToAdd); // safe overflow
        return resolvePreviousValid(newYear, monthOfYear, dayOfMonth);
    }

    public SolarHijriDate plusMonths(long monthsToAdd)
    {
        if (monthsToAdd == 0)
            return this;
        long monthCount = year * 12L + (monthOfYear - 1);
        long calcMonths = monthCount + monthsToAdd;  // safe overflow
        int newYear = YEAR.checkValidIntValue(Math.floorDiv(calcMonths, 12));
        int newMonth = (int) Math.floorMod(calcMonths, 12) + 1;
        return resolvePreviousValid(newYear, newMonth, dayOfMonth);
    }

    public SolarHijriDate plusWeeks(long weeksToAdd)
    {
        return plusDays(Math.multiplyExact(weeksToAdd, 7));
    }

    public SolarHijriDate plusDays(long daysToAdd)
    {
        if (daysToAdd == 0)
            return this;
        long mjDay = Math.addExact(toEpochDay(), daysToAdd);
        return SolarHijriDate.ofEpochDay(mjDay);
    }

    @Override
    public SolarHijriDate minus(TemporalAmount amount)
    {
        return (SolarHijriDate) ChronoLocalDate.super.minus(amount);
    }

    @Override
    public SolarHijriDate minus(long amountToSubtract, TemporalUnit unit)
    {
        return (SolarHijriDate) ChronoLocalDate.super.minus(amountToSubtract, unit);
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit)
    {
        SolarHijriDate end = getChronology().date(endExclusive);
        if (unit instanceof ChronoUnit) {
            switch ((ChronoUnit) unit) {
                case DAYS:
                    return daysUntil(end);
                case WEEKS:
                    return daysUntil(end) / 7;
                case MONTHS:
                    return monthsUntil(end);
                case YEARS:
                    return monthsUntil(end) / 12;
                case DECADES:
                    return monthsUntil(end) / 120;
                case CENTURIES:
                    return monthsUntil(end) / 1200;
                case MILLENNIA:
                    return monthsUntil(end) / 12000;
                case ERAS:
                    return end.getLong(ERA) - getLong(ERA);
            }
            throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
        }
        return unit.between(this, end);
    }

    @Override
    public ChronoPeriod until(ChronoLocalDate endDateExclusive)
    {
        SolarHijriDate end = SolarHijriDate.from(endDateExclusive);
        long totalMonths = end.getProlepticMonth() - this.getProlepticMonth(); // safe
        int days = end.dayOfMonth - this.dayOfMonth;
        if (totalMonths > 0 && days < 0) {
            totalMonths--;
            SolarHijriDate calcDate = this.plusMonths(totalMonths);
            days = (int) (end.toEpochDay() - calcDate.toEpochDay()); // safe
        } else if (totalMonths < 0 && days > 0) {
            totalMonths++;
            days -= end.lengthOfMonth();
        }
        long years = totalMonths / 12; // safe
        int months = (int) (totalMonths % 12); // safe
        return getChronology().period(Math.toIntExact(years), months, days);
    }

    private long daysUntil(SolarHijriDate end)
    {
        return end.toEpochDay() - toEpochDay(); // no overflow
    }

    private long monthsUntil(SolarHijriDate end)
    {
        long packed1 = getProlepticMonth() * 32L + dayOfMonth; // no overflow
        long packed2 = end.getProlepticMonth() * 32L + end.dayOfMonth; // no overflow
        return (packed2 - packed1) / 32;
    }

    @Override
    public long getLong(TemporalField field)
    {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case DAY_OF_WEEK:
                    return getDayOfWeek();
                case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                    return (dayOfMonth - 1) % 7 + 1;
                case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                    return (getDayOfYear() - 1) % 7 + 1;
                case DAY_OF_MONTH:
                    return dayOfMonth;
                case DAY_OF_YEAR:
                    return getDayOfYear();
                case EPOCH_DAY:
                    return toEpochDay();
                case ALIGNED_WEEK_OF_MONTH:
                    return (dayOfMonth - 1) / 7 + 1;
                case ALIGNED_WEEK_OF_YEAR:
                    return (getDayOfYear() - 1) / 7 + 1;
                case MONTH_OF_YEAR:
                    return monthOfYear;
                case PROLEPTIC_MONTH:
                    return getProlepticMonth();
                case YEAR_OF_ERA:
                    return year >= 1 ? year : 1 - year;
                case YEAR:
                    return year;
                case ERA:
                    return year >= 1 ? 1 : 0;
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        return field.getFrom(this);
    }

    @Override // override for performance
    public int compareTo(ChronoLocalDate other)
    {
        if (other instanceof SolarHijriDate)
            return compareTo((SolarHijriDate) other);
        return ChronoLocalDate.super.compareTo(other);
    }

    private int compareTo(SolarHijriDate other)
    {
        int cmp = year - other.year;
        if (cmp == 0) {
            cmp = monthOfYear - other.monthOfYear;
            if (cmp == 0)
                cmp = dayOfMonth - other.dayOfMonth;
        }
        return cmp;
    }

    @Override // override for performance
    public boolean isAfter(ChronoLocalDate other)
    {
        if (other instanceof SolarHijriDate)
            return compareTo((SolarHijriDate) other) > 0;
        return ChronoLocalDate.super.isAfter(other);
    }

    @Override // override for performance
    public boolean isBefore(ChronoLocalDate other)
    {
        if (other instanceof SolarHijriDate)
            return compareTo((SolarHijriDate) other) < 0;
        return ChronoLocalDate.super.isBefore(other);
    }

    @Override // override for performance
    public boolean isEqual(ChronoLocalDate other)
    {
        if (other instanceof SolarHijriDate)
            return compareTo((SolarHijriDate) other) == 0;
        return ChronoLocalDate.super.isEqual(other);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj instanceof SolarHijriDate)
            return compareTo((SolarHijriDate) obj) == 0;
        return false;
    }

    @Override
    public int hashCode()
    {
        int month = monthOfYear;
        int day = dayOfMonth;
        return getChronology().getId().hashCode() ^ (year & 0xFFFFF800) ^ ((year << 11) + (month << 6) + day);
    }

    @Override
    public String toString()
    {
        long yoe = getLong(YEAR_OF_ERA);
        long moy = getLong(MONTH_OF_YEAR);
        long dom = getLong(DAY_OF_MONTH);
        return new StringBuilder(30)
                .append(getChronology().toString())
                .append(" ").append(getEra())
                .append(" ").append(yoe)
                .append(moy < 10 ? "-0" : "-").append(moy)
                .append(dom < 10 ? "-0" : "-").append(dom)
                .toString();
    }

}
