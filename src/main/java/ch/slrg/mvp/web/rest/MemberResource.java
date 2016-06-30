package ch.slrg.mvp.web.rest;

import ch.slrg.mvp.domain.Assessment;
import ch.slrg.mvp.repository.AssessmentRepository;
import ch.slrg.mvp.security.AuthoritiesConstants;
import ch.slrg.mvp.service.AppearancesService;
import ch.slrg.mvp.service.EducationService;
import ch.slrg.mvp.service.FurtherEducationService;
import ch.slrg.mvp.web.rest.dto.AppearancesDTO;
import ch.slrg.mvp.web.rest.dto.EducationDTO;
import ch.slrg.mvp.web.rest.dto.FurtherEducationDTO;
import com.codahale.metrics.annotation.Timed;
import ch.slrg.mvp.domain.Member;
import ch.slrg.mvp.service.MemberService;
import ch.slrg.mvp.web.rest.util.HeaderUtil;
import ch.slrg.mvp.web.rest.util.PaginationUtil;
import ch.slrg.mvp.web.rest.dto.MemberDTO;
import ch.slrg.mvp.web.rest.mapper.MemberMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing Member.
 */
@RestController
@RequestMapping("/api")
public class MemberResource {

    private final Logger log = LoggerFactory.getLogger(MemberResource.class);

    @Inject
    private MemberService memberService;

    @Inject
    private MemberMapper memberMapper;

    @Inject
    private EducationService educationService;

    @Inject
    private AppearancesService appearancesService;

    @Inject
    private FurtherEducationService furtherEducationService;

    @Inject
    private AssessmentRepository assessmentRepository;


    /**
     * POST  /members : Create a new member.
     *
     * @param memberDTO the memberDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new memberDTO, or with status 400 (Bad Request) if the member has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/members",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<MemberDTO> createMember(@RequestBody MemberDTO memberDTO) throws URISyntaxException {
        log.debug("REST request to save Member : {}", memberDTO);
        if (memberDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("member", "idexists", "A new member cannot already have an ID")).body(null);
        }
        MemberDTO result = memberService.save(memberDTO);
        return ResponseEntity.created(new URI("/api/members/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("member", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /members : Updates an existing member.
     *
     * @param memberDTO the memberDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated memberDTO,
     * or with status 400 (Bad Request) if the memberDTO is not valid,
     * or with status 500 (Internal Server Error) if the memberDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/members",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<MemberDTO> updateMember(@RequestBody MemberDTO memberDTO) throws URISyntaxException {
        log.debug("REST request to update Member : {}", memberDTO);
        if (memberDTO.getId() == null) {
            return createMember(memberDTO);
        }
        MemberDTO result = memberService.save(memberDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("member", memberDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /members : get all the members.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of members in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/members",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<MemberDTO>> getAllMembers(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Members");
        Page<Member> page = memberService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/members");
        return new ResponseEntity<>(memberMapper.membersToMemberDTOs(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * GET  /members/:id : get the "id" member.
     *
     * @param id the id of the memberDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the memberDTO, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/members/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<MemberDTO> getMember(@PathVariable Long id) {
        log.debug("REST request to get Member : {}", id);
        MemberDTO memberDTO = memberService.findOne(id);
        return Optional.ofNullable(memberDTO)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /members/:id : delete the "id" member.
     *
     * @param id the id of the memberDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/members/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        log.debug("REST request to delete Member : {}", id);
        memberService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("member", id.toString())).build();
    }

    /**
     * GET  /members/:id/education : delete the "id" member.
     *
     * @param id the id of the Member to load the Educations from
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/members/{id}/educations",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.USER)
    public ResponseEntity<List<EducationDTO>> getListEducation(@PathVariable Long id) {
        log.debug("REST request to find Member Education : {}", id);
        List<EducationDTO> dtos = educationService.findEducationsByMember(id);
        return Optional.ofNullable(dtos)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * GET  /members/:id/appearances :  get all Appearances by one member.
     *
     * @param id the id of the Member to load the Educations from
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/members/{id}/appearances",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.USER)
    public ResponseEntity<List<AppearancesDTO>> getListAppearances(@PathVariable Long id) {
        log.debug("REST request to find Member Appearances : {}", id);
        List<AppearancesDTO> dtos = appearancesService.findAppearancesByMemberId(id);
        return Optional.ofNullable(dtos)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


    /**
     * GET  /members/:id/furtheredu :  get all further Educations by one member.
     *
     * @param id the id of the Member to load the Educations from
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/members/{id}/furtheredu",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.USER)
    public ResponseEntity<List<FurtherEducationDTO>> getListFurtherEdu(@PathVariable Long id) {
        log.debug("REST request to find Member Appearances : {}", id);
        List<FurtherEducationDTO> dtos = furtherEducationService.findFurtherEducationByMemberId(id);
        return Optional.ofNullable(dtos)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * GET  /members/:id/assessments :  get all further Educations by one member.
     *
     * @param id the id of the Member to load the Educations from
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/members/{id}/assessments",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @Secured(AuthoritiesConstants.USER)
    public ResponseEntity<List<Assessment>> getAssessments(@PathVariable Long id) {
        log.debug("REST request to find Member Assessments : {}", id);
        List<Assessment> dtos = assessmentRepository.findAssessmentByMemberId(id);
        return Optional.ofNullable(dtos)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


}
