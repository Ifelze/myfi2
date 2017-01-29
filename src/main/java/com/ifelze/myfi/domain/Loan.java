package com.ifelze.myfi.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.elasticsearch.annotations.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ifelze.myfi.constant.LoanStatus;
import com.ifelze.myfi.constant.LoanType;

/**
 * A user.
 */
@Entity
@Table(name = "mf_loan")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Document(indexName = "loan")
public class Loan extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name="loan_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @NotNull
    @Column(name="loan_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatus;
    
    @Column(name = "loan_amount")
    private double loanAmount;

    @JsonIgnore
    @ManyToMany
    @JoinTable( 
        name = "mf_loan_agent",
        joinColumns = {@JoinColumn(name = "loan_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "agent_id", referencedColumnName = "id")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<User> agents = new HashSet<>();
    
    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "mf_loan_processor",
        joinColumns = {@JoinColumn(name = "loan_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "processor_id", referencedColumnName = "id")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<User> processors = new HashSet<>();
    
    
    @JsonIgnore
    @ManyToMany
    @JoinTable(
        name = "mf_loan_user",
        joinColumns = {@JoinColumn(name = "loan_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<User> users = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}