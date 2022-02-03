# JSR-310 Solar Hijri Chronology
A JSR-310 implementation of the Solar Hijri calendar system, also called the Solar Hejri calendar or Shamsi Hijri calendar.

## Maven dependency

```xml
<dependency>
    <groupId>ir.unilogic.common</groupId>
    <artifactId>solarhijri-jsr310</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Sample usage

```java
LocalDate isoDate = IsoChronology.INSTANCE.dateNow();
SolarHijriDate shDate = SolarHijriChronology.INSTANCE.date(isoDate);
assertEquals(IsoChronology.INSTANCE.date(shDate), isoDate);
assertTrue(shDate.isEqual(isoDate));
assertTrue(isoDate.isEqual(shDate));

DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        .withChronology(SolarHijriChronology.INSTANCE);
assertEquals(LocalDate.parse("2021-03-21"), LocalDate.from(formatter.parse("1400-01-01")));
```
