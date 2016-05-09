package nl.erasmusmc.bios.clef.icd10;

public class TagItem {
    private Integer start = null;
    private Integer end = null;
    private String uuid = null;
    private String icd10 = null;
    private String term = null;
    public Integer getStart() {
	return start;
    }
    public void setStart(Integer start) {
	this.start = start;
    }
    public Integer getEnd() {
	return end;
    }
    public void setEnd(Integer end) {
	this.end = end;
    }
    public String getUuid() {
	return uuid;
    }
    public void setUuid(String uuid) {
	this.uuid = uuid;
    }
    public String getIcd10() {
	return icd10;
    }
    public void setIcd10(String icd10) {
	this.icd10 = icd10;
    }
    public String getTerm() {
	return term;
    }
    public void setTerm(String term) {
	this.term = term;
    }
    
    @Override
    public String toString(){
	return icd10+":"+start+":"+end;
    }
}
