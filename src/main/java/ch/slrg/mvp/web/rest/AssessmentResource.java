package ch.slrg.mvp.web.rest;

import com.codahale.metrics.annotation.Timed;
import ch.slrg.mvp.domain.Assessment;
import ch.slrg.mvp.repository.AssessmentRepository;
import ch.slrg.mvp.web.rest.util.HeaderUtil;
import ch.slrg.mvp.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Assessment.
 */
@RestController
@RequestMapping("/api")
public class AssessmentResource {

    private final Logger log = LoggerFactory.getLogger(AssessmentResource.class);
        
    @Inject
    private AssessmentRepository assessmentRepository;
    
    /**
     * POST  /assessments : Create a new assessment.
     *
     * @param assessment the assessment to create
     * @return the ResponseEntity with status 201 (Created) and with body the new assessment, or with status 400 (Bad Request) if the assessment has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/assessments",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Assessment> createAssessment(@RequestBody Assessment assessment) throws URISyntaxException {
        log.debug("REST request to save Assessment : {}", assessment);
        if (assessment.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("assessment", "idexists", "A new assessment cannot already have an ID")).body(null);
        }
        Assessment result = assessmentRepository.save(assessment);
        return ResponseEntity.created(new URI("/api/assessments/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("assessment", result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /assessments : Updates an existing assessment.
     *
     * @param assessment the assessment to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated assessment,
     * or with status 400 (Bad Request) if the assessment is not valid,
     * or with status 500 (Internal Server Error) if the assessment couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @RequestMapping(value = "/assessments",
        method = RequestMethod.PUT,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Assessment> updateAssessment(@RequestBody Assessment assessment) throws URISyntaxException {
        log.debug("REST request to update Assessment : {}", assessment);
        if (assessment.getId() == null) {
            return createAssessment(assessment);
        }
        Assessment result = assessmentRepository.save(assessment);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("assessment", assessment.getId().toString()))
            .body(result);
    }

    /**
     * GET  /assessments : get all the assessments.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of assessments in body
     * @throws URISyntaxException if there is an error to generate the pagination HTTP headers
     */
    @RequestMapping(value = "/assessments",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<Assessment>> getAllAssessments(Pageable pageable)
        throws URISyntaxException {
        log.debug("REST request to get a page of Assessments");
        Page<Assessment> page = assessmentRepository.findAll(pageable); 
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/assessments");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /assessments/:id : get the "id" assessment.
     *
     * @param id the id of the assessment to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the assessment, or with status 404 (Not Found)
     */
    @RequestMapping(value = "/assessments/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Assessment> getAssessment(@PathVariable Long id) {
        log.debug("REST request to get Assessment : {}", id);
        Assessment assessment = assessmentRepository.findOne(id);
        return Optional.ofNullable(assessment)
            .map(result -> new ResponseEntity<>(
                result,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /assessments/:id : delete the "id" assessment.
     *
     * @param id the id of the assessment to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @RequestMapping(value = "/assessments/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> deleteAssessment(@PathVariable Long id) {
        log.debug("REST request to delete Assessment : {}", id);
        assessmentRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("assessment", id.toString())).build();
    }

}
