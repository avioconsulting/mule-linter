package org.avioconsulting.maven.mulelinter.test;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import org.apache.maven.execution.MavenExecutionResult;
import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

@MavenJupiterExtension // <1>
public class MuleLinterTest2 {

    @MavenTest
    void the_first_test_case(MavenExecutionResult result) { //<3>
        assertThat(result).isNull();
    }

}
