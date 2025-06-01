package com.hyperformancelabs.backend.service.impl;

import com.hyperformancelabs.backend.model.TermsAndConditions;
import com.hyperformancelabs.backend.repository.TermsAndConditionsRepository;
import com.hyperformancelabs.backend.service.TermsAndConditionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TermsAndConditionsServiceImpl implements TermsAndConditionsService {

    private final TermsAndConditionsRepository termsRepository;
    
    // Default content if none exists in the database
    private static final String DEFAULT_CONTENT = """
            # 1. Giới thiệu
            
            Chào mừng bạn đến với APH Perfume - nơi cung cấp những sản phẩm nước hoa cao cấp. Bằng việc truy cập hoặc sử dụng trang web của chúng tôi, bạn đồng ý tuân thủ và chịu ràng buộc bởi các điều khoản và điều kiện sau đây.
            
            # 2. Tài khoản người dùng
            
            Khi bạn đăng ký tài khoản trên trang web của chúng tôi, bạn phải cung cấp thông tin chính xác, đầy đủ và cập nhật. Việc không làm như vậy có thể dẫn đến việc hủy tài khoản của bạn.
            
            Bạn chịu trách nhiệm bảo mật tài khoản của mình, bao gồm mật khẩu, và chịu trách nhiệm cho tất cả hoạt động diễn ra dưới tài khoản của bạn. Bạn phải thông báo cho chúng tôi ngay lập tức nếu phát hiện bất kỳ vi phạm bảo mật nào.
            
            # 3. Đơn hàng và thanh toán
            
            Khi đặt hàng, bạn đồng ý cung cấp thông tin thanh toán chính xác và có quyền sử dụng phương thức thanh toán đã chọn.
            
            Giá sản phẩm có thể thay đổi mà không cần thông báo trước. Chúng tôi có quyền từ chối hoặc hủy bất kỳ đơn đặt hàng nào vì bất kỳ lý do gì.
            
            # 4. Vận chuyển và giao hàng
            
            Thời gian giao hàng là ước tính và không được đảm bảo. Chúng tôi không chịu trách nhiệm về bất kỳ sự chậm trễ nào trong quá trình vận chuyển.
            
            # 5. Chính sách đổi trả
            
            Bạn có thể trả lại sản phẩm trong vòng 7 ngày kể từ ngày nhận hàng nếu sản phẩm còn nguyên vẹn và chưa sử dụng. Vui lòng liên hệ với chúng tôi để biết thêm chi tiết về quy trình đổi trả.
            
            # 6. Quyền sở hữu trí tuệ
            
            Tất cả nội dung trên trang web của chúng tôi, bao gồm nhưng không giới hạn ở văn bản, hình ảnh, biểu tượng, logo và mã, đều thuộc sở hữu của chúng tôi và được bảo vệ bởi luật bản quyền.
            
            # 7. Luật áp dụng
            
            Các điều khoản và điều kiện này được điều chỉnh và giải thích theo luật pháp Việt Nam. Bất kỳ tranh chấp nào phát sinh từ hoặc liên quan đến việc sử dụng trang web của chúng tôi sẽ được giải quyết tại các tòa án có thẩm quyền tại Việt Nam.
            """;

    @Autowired
    public TermsAndConditionsServiceImpl(TermsAndConditionsRepository termsRepository) {
        this.termsRepository = termsRepository;
    }
    
    @Override
    public TermsAndConditions getLatestTerms() {
        TermsAndConditions terms = termsRepository.findFirstByOrderByLastUpdatedDesc();
        
        // If no terms exist, create default terms
        if (terms == null) {
            terms = new TermsAndConditions();
            terms.setMarkdownContent(DEFAULT_CONTENT);
            return termsRepository.save(terms);
        }
        
        return terms;
    }
    
    @Override
    public TermsAndConditions updateTerms(String markdownContent) {
        TermsAndConditions terms = getLatestTerms();
        terms.setMarkdownContent(markdownContent);
        return termsRepository.save(terms);
    }
    
    @Override
    public Date getLastUpdatedDate() {
        TermsAndConditions terms = getLatestTerms();
        return terms.getLastUpdated();
    }
} 