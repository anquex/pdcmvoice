% Marco Bettiol - 586580 - BATCH SIZE ESTIMATE
%
% CBT Full Test -> Latex

clear all;
close all;
clc;

load cbt_full_test

% input loaeded from file
%logn_max=15;
%c=100000;

% generate the test batch size
%x=1:logn_max;
%testsizes=2.^x;

estimatesizes=2.^[1:logn_max+40];

ED=ED/c;

TABLE=ED;
[rows,cols]=size(TABLE);
rownames=testsizes;
colnames=estimatesizes;

%split columns in sets
cdelimiter=[1,12,23,33];
ii=1;
for d=1:length(cdelimiter)-1
    min=cdelimiter(d);
    max=cdelimiter(d+1)-1;
    %split the big table by column sets
    fprintf('\nTABLE (%d)\n\n',ii);
    ii=ii+1;
    %BEGIN TABULAR
    fprintf('\\begin{tabular}{');
    for i=min:(max)+2
        fprintf('c');
    end
    fprintf('}\n');
    %HEADINGS
    fprintf('n&$\\hat{n}:$&');
    for i=min:max-1
        fprintf('%d&',colnames(i));
    end
    fprintf('%d \\\\\\hline\r\n',colnames(max));
    %BODY
    for h=1:rows
        fprintf('%d &&',rownames(h));
        for i=min:1:max-1
            if (TABLE(h, i)==0)
                fprintf('&');
            else
                fprintf(['%' formatme(TABLE(h, i)) ' &'], TABLE(h, i)); 
            end
        end
        fprintf(['%' formatme(TABLE(h, max)) '\\\\\\hline\r\n'], TABLE(h, max));
    
    end
    %END TABULAR
    fprintf('\\end{tabular}\n\n');
end