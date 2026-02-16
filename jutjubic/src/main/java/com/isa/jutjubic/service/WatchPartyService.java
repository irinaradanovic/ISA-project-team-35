// [jutjubic/src/main/java/com/isa/jutjubic/service/WatchPartyService.java](jutjubic/src/main/java/com/isa/jutjubic/service/WatchPartyService.java)
package com.isa.jutjubic.service;

import com.isa.jutjubic.dto.WatchPartyCreateRequestDto;
import com.isa.jutjubic.dto.WatchPartyDto;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.model.WatchParty;
import com.isa.jutjubic.repository.UserRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import com.isa.jutjubic.repository.WatchPartyRepository;
import com.isa.jutjubic.security.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class WatchPartyService {

    @Autowired
    private WatchPartyRepository watchPartyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Transactional
    public WatchPartyDto createRoom(WatchPartyCreateRequestDto request) {
        if (request == null || request.getVideoId() == null) {
            throw new IllegalArgumentException("videoId is required");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        VideoPost video = videoPostRepository.findById(request.getVideoId())
                .orElseThrow(() -> new NoSuchElementException("Video not found"));

        String roomName = request.getRoomName();
        if (roomName == null || roomName.trim().isEmpty()) {
            roomName = "Watch Party : " + video.getTitle();
        }

        WatchParty room = new WatchParty();
        room.setRoomName(roomName.trim());
        room.setCreator(creator);
        room.setCurrentVideo(video);
        room.setActive(false);

        return toDto(watchPartyRepository.save(room));
    }

    public WatchPartyDto getRoom(Long id) {
        WatchParty room = watchPartyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Watch party not found"));
        return toDto(room);
    }

    @Transactional
    public WatchPartyDto markStarted(Long roomId, Integer videoId) {
        WatchParty room = watchPartyRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Watch party not found"));

        if (videoId != null) {
            VideoPost video = videoPostRepository.findById(videoId)
                    .orElseThrow(() -> new NoSuchElementException("Video not found"));
            room.setCurrentVideo(video);
        }

        room.setActive(true);
        return toDto(room);
    }

    @Transactional
    public WatchPartyDto markStopped(Long roomId) {
        WatchParty room = watchPartyRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("Watch party not found"));
        room.setActive(false);
        return toDto(room);
    }

    private WatchPartyDto toDto(WatchParty room) {
        Integer currentVideoId = room.getCurrentVideo() != null ? room.getCurrentVideo().getId() : null;
        return WatchPartyDto.builder()
                .id(room.getId())
                .roomName(room.getRoomName())
                .creatorId(room.getCreator().getId())
                .currentVideoId(currentVideoId)
                .active(room.isActive())
                .build();
    }
}