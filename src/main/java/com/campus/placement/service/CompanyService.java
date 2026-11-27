package com.campus.placement.service;

import com.campus.placement.dao.CompanyDao;
import com.campus.placement.dao.JdbcCompanyDao;
import com.campus.placement.exception.DataAccessException;
import com.campus.placement.exception.DuplicateEntryException;
import com.campus.placement.exception.ValidationException;
import com.campus.placement.model.Company;
import com.campus.placement.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

/**
 * Service managing recruiting {@link Company} records.
 */
public class CompanyService {

    private final CompanyDao companyDao;
    private final AuditService auditService;

    public CompanyService() {
        this(new JdbcCompanyDao(), new AuditService());
    }

    public CompanyService(CompanyDao companyDao, AuditService auditService) {
        this.companyDao = companyDao;
        this.auditService = auditService;
    }

    public List<Company> getAllCompanies() throws DataAccessException {
        return companyDao.findAll();
    }

    public List<Company> searchCompanies(String keyword) throws DataAccessException {
        return companyDao.search(keyword);
    }

    public Company createCompany(Company company, Integer actorUserId)
            throws ValidationException, DuplicateEntryException, DataAccessException {
        validate(company);
        Optional<Company> existing = companyDao.findByName(company.getName().trim());
        if (existing.isPresent()) {
            throw new DuplicateEntryException("Company already exists: " + company.getName());
        }
        company.setName(company.getName().trim());
        Company saved = companyDao.save(company);
        auditService.log(actorUserId, "CREATE", "COMPANY", saved.getId(), "Created " + saved.getName());
        return saved;
    }

    public void updateCompany(Company company, Integer actorUserId)
            throws ValidationException, DuplicateEntryException, DataAccessException {
        validate(company);
        Optional<Company> existing = companyDao.findByName(company.getName().trim());
        if (existing.isPresent() && existing.get().getId() != company.getId()) {
            throw new DuplicateEntryException("Another company already has name: " + company.getName());
        }
        company.setName(company.getName().trim());
        companyDao.update(company);
        auditService.log(actorUserId, "UPDATE", "COMPANY", company.getId(), "Updated " + company.getName());
    }

    public void deleteCompany(int companyId, Integer actorUserId) throws DataAccessException {
        companyDao.deleteById(companyId);
        auditService.log(actorUserId, "DELETE", "COMPANY", companyId, "Deleted company id=" + companyId);
    }

    private void validate(Company c) throws ValidationException {
        ValidationUtil.requireNonBlank(c.getName(), "Company Name");
        ValidationUtil.requireNonBlank(c.getIndustry(), "Industry");
        ValidationUtil.requireNonBlank(c.getContactPerson(), "Contact Person");
        ValidationUtil.validateEmail(c.getContactEmail());
        ValidationUtil.validatePhone(c.getHrPhone());
    }
}
