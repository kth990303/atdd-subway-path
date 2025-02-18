package wooteco.subway.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wooteco.subway.dao.LineDao;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.dao.StationDao;
import wooteco.subway.domain.*;
import wooteco.subway.dto.PathRequest;
import wooteco.subway.dto.PathResponse;
import wooteco.subway.service.pathInfra.ShortestPathFinder;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Transactional(readOnly = true)
@Service
public class PathService {
    private final StationDao stationDao;
    private final LineDao lineDao;
    private final SectionDao sectionDao;

    public PathService(StationDao stationDao, LineDao lineDao, SectionDao sectionDao) {
        this.stationDao = stationDao;
        this.lineDao = lineDao;
        this.sectionDao = sectionDao;
    }

    public PathResponse findShortestPath(PathRequest pathRequest) {
        validateNotSameStations(pathRequest.getSource(), pathRequest.getTarget());
        final Path shortestPath = makePath(pathRequest.getSource(), pathRequest.getTarget());

        final List<Station> stations = shortestPath.getStations();
        final int shortestDistance = shortestPath.getDistance();
        final int extraFare = findMaximumExtraFare(shortestPath);
        final Fare fare = Fare.of(shortestDistance, extraFare, pathRequest.getAge());
        return new PathResponse(stations, shortestDistance, fare.getValue());
    }

    private Path makePath(Long source, Long target) {
        final PathFinder pathFinder = new ShortestPathFinder();
        final List<Station> stations = stationDao.findAll();
        final List<Section> sections = sectionDao.findAll();
        makeGraph(pathFinder, stations, sections);
        return pathFinder.findShortestPathByGraph(findStation(source), findStation(target));
    }

    private void makeGraph(PathFinder pathFinder, List<Station> stations, List<Section> sections) {
        pathFinder.addVertex(stations);
        for (Section section : sections) {
            final Station upStation = findStation(section.getUpStationId());
            final Station downStation = findStation(section.getDownStationId());
            pathFinder.addEdge(upStation, downStation, section);
        }
    }

    private Station findStation(Long id) {
        return stationDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 역이 존재하지 않습니다."));
    }

    private void validateNotSameStations(Long source, Long target) {
        if (Objects.equals(source, target)) {
            throw new IllegalArgumentException("출발역과 도착역이 같을 수 없습니다.");
        }
    }

    private int findMaximumExtraFare(Path shortestPath) {
        return lineDao.findByIds(shortestPath.getIncludeLineIds())
                .stream()
                .max(Comparator.comparingInt(Line::getExtraFare))
                .orElseThrow(() -> new NoSuchElementException("추가 요금을 찾는 과정 중 오류가 발생했습니다."))
                .getExtraFare();
    }
}
