package wooteco.subway.service.fakeDao;

import org.springframework.util.ReflectionUtils;
import wooteco.subway.dao.SectionDao;
import wooteco.subway.domain.Section;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SectionDaoImpl implements SectionDao {
    private static final SectionDaoImpl sectionDao = new SectionDaoImpl();
    private static final List<Section> sections = new ArrayList<>();
    private static Long seq = 0L;

    public static SectionDaoImpl getInstance() {
        return sectionDao;
    }

    @Override
    public Long save(Section section) {
        Section persistSection = createNewObject(section);
        sections.add(persistSection);
        return persistSection.getId();
    }

    @Override
    public Optional<Section> findById(Long id) {
        return sections.stream()
                .filter(section -> Objects.equals(section.getId(), id))
                .findFirst();
    }

    @Override
    public List<Section> findByLineId(Long lineId) {
        return sections.stream()
                .filter(section -> Objects.equals(section.getLineId(), lineId))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public List<Section> findAll() {
        return sections;
    }

    @Override
    public Optional<Section> findByStationId(Long stationId) {
        return sections.stream()
                .filter(it -> Objects.equals(it.getUpStationId(), stationId) ||
                        Objects.equals(it.getDownStationId(), stationId))
                .findFirst();
    }

    @Override
    public Optional<Section> findBySameUpOrDownStationId(Long lineId, Section section) {
        List<Section> sections = findByLineId(lineId);
        return sections.stream()
                .filter(it -> Objects.equals(it.getUpStationId(), section.getUpStationId()) ||
                        Objects.equals(it.getDownStationId(), section.getDownStationId()))
                .findFirst();
    }

    @Override
    public Optional<Section> findByUpStationId(Long lineId, Long upStationId) {
        return findByLineId(lineId).stream()
                .filter(it -> Objects.equals(it.getUpStationId(), upStationId))
                .findFirst();
    }

    @Override
    public Optional<Section> findByDownStationId(Long lineId, Long downStationId) {
        return findByLineId(lineId).stream()
                .filter(it -> Objects.equals(it.getDownStationId(), downStationId))
                .findFirst();
    }

    @Override
    public void updateDownStation(Long id, Long downStationId, int newDistance) {
        Section findSection = findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + "번에 해당하는 구간이 존재하지 않습니다."));
        sections.remove(findSection);
        Section updateSection =
                new Section(id, findSection.getLineId(), findSection.getUpStationId(), downStationId, newDistance);
        sections.add(updateSection);
    }

    @Override
    public void updateUpStation(Long id, Long upStationId, int newDistance) {
        Section findSection = findById(id)
                .orElseThrow(() -> new IllegalArgumentException(id + "번에 해당하는 구간이 존재하지 않습니다."));
        sections.remove(findSection);
        Section updateSection =
                new Section(id, findSection.getLineId(), upStationId, findSection.getDownStationId(), newDistance);
        sections.add(updateSection);
    }

    @Override
    public void delete(List<Section> deleteSections) {
        for (Section deleteSection : deleteSections) {
            deleteById(deleteSection.getId());
        }
    }

    private void deleteById(Long deleteSectionId) {
        boolean result = sections.removeIf(section -> Objects.equals(section.getId(), deleteSectionId));
        if (!result) {
            throw new IllegalArgumentException(deleteSectionId + "번에 해당하는 노선이 존재하지 않습니다.");
        }
    }

    private Section createNewObject(Section section) {
        Field field = ReflectionUtils.findField(Section.class, "id");
        field.setAccessible(true);
        ReflectionUtils.setField(field, section, ++seq);
        return section;
    }
}
