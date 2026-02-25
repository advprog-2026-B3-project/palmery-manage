package id.ac.ui.cs.advprog.palmerymanage.service;

import id.ac.ui.cs.advprog.palmerymanage.model.FotoPanen;
import id.ac.ui.cs.advprog.palmerymanage.model.HasilPanen;
import id.ac.ui.cs.advprog.palmerymanage.repository.FotoPanenRepository;
import id.ac.ui.cs.advprog.palmerymanage.repository.HasilPanenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Service
public class PanenService {

    @Autowired
    private HasilPanenRepository hasilPanenRepository;

    @Autowired
    private FotoPanenRepository fotoPanenRepository;

    public FotoPanen uploadFotoBukti(UUID hasilPanenId, MultipartFile file) throws Exception {
        HasilPanen panen = hasilPanenRepository.findById(hasilPanenId)
                .orElseThrow(() -> new Exception("Data Panen tidak ditemukan!"));

        //dummy
        String mockRustfsUrl = "https://rustfs.palmery.my.id/uploads/" + file.getOriginalFilename();

        FotoPanen foto = new FotoPanen();
        foto.setHasilPanen(panen);
        foto.setUrl(mockRustfsUrl);
        foto.setFilename(file.getOriginalFilename());
        foto.setSizeBytes((int) file.getSize());

        return fotoPanenRepository.save(foto);
    }
}