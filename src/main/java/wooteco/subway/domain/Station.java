package wooteco.subway.domain;

import java.util.Objects;

public class Station {
    private final Long id;
    private final String name;

    public Station(Long id, String name) {
        this.id = id;
        this.name = name;
        validateStation(name);
    }

    public Station(String name) {
        this(null, name);
    }

    private void validateStation(String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("이름은 빈 값일 수 없습니다.");
        }
    }

    public boolean isSameStation(Long stationId) {
        return Objects.equals(id, stationId);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return Objects.equals(id, station.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

