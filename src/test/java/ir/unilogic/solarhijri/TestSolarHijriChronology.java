/*
 * Copyright 2022 UNILOGIC.IR
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
package ir.unilogic.solarhijri;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.IsoChronology;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import static java.time.temporal.ChronoUnit.*;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import static org.junit.Assert.*;
import org.junit.Test;

public class TestSolarHijriChronology
{
    @Test
    public void testConversion()
    {
        LocalDate isoNow = IsoChronology.INSTANCE.dateNow();
        SolarHijriDate now = SolarHijriChronology.INSTANCE.date(isoNow);
        assertEquals(IsoChronology.INSTANCE.date(now), isoNow);
        assertTrue(now.isEqual(isoNow));
        assertTrue(isoNow.isEqual(now));
        assertFalse(now.isAfter(isoNow));
        assertFalse(isoNow.isBefore(now));
    }

    @Test
    public void testManipulation()
    {
        Random rand = ThreadLocalRandom.current();
        ChronoLocalDate now = SolarHijriChronology.INSTANCE.dateNow();
        ChronoUnit[] units = {DAYS, WEEKS, MONTHS, YEARS};

        for (ChronoUnit unit : units) {
            long amount = rand.nextInt(10000 / (int) unit.getDuration().toDays()) + 1L;
            ChronoLocalDate future = now.plus(amount, unit);
            ChronoLocalDate past = now.minus(amount, unit);

            try {
                assertTrue(future.minus(amount, unit).isEqual(now));
                assertTrue(past.plus(amount, unit).isEqual(now));
                assertEquals(amount, now.until(future, unit));
                assertEquals(-amount, now.until(past, unit));
            } catch (AssertionError ex) {
                System.err.printf("Assertion failed. unit=%s, amount=%s", unit, amount).println();
                throw ex;
            }
        }
    }

    /**
     * Check accuracy against ICU calendar.
     */
    @Test
    public void testAccuracy()
    {
        Calendar cal = Calendar.getInstance(ULocale.forLanguageTag("fa-IR@calendar=persian"));

        // From 0002-01-01 BH (Gregorian 0620-03-21) to 1479-01-01 AH (Gregorian 2100-03-21)
        for (int day = -492998; day <= 47561; day++) {
            SolarHijriDate date = SolarHijriChronology.INSTANCE.dateEpochDay(day);
            LocalDate isoDate = IsoChronology.INSTANCE.date(date);
            cal.setTime(Date.from(isoDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));

            try {
                assertEquals(date.getLong(ChronoField.YEAR), cal.get(Calendar.YEAR));
                assertEquals(date.getLong(ChronoField.MONTH_OF_YEAR), cal.get(Calendar.MONTH) + 1);
                assertEquals(date.getLong(ChronoField.DAY_OF_MONTH), cal.get(Calendar.DAY_OF_MONTH));
            } catch (AssertionError ex) {
                System.err.printf("Assertion failed. isoDate=%s, actual=%s, expected=%s",
                        isoDate, format(date), format(cal)).println();
                throw ex;
            }
        }
    }

    private static String format(SolarHijriDate date)
    {
        return format(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }

    private static String format(Calendar cal)
    {
        return format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    private static String format(int year, int month, int day)
    {
        return new StringBuilder().append(year)
                .append(month < 10 ? "/0" : "/").append(month)
                .append(day < 10 ? "/0" : "/").append(day)
                .toString();
    }
}
