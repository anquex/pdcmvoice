disp(' ');
disp('(Expected value)');
disp('Latex Output:');
disp(' ');
%output for latex, 5 column, decimal dot alignment
fprintf(' n & \\multicolumn{2}{r}{$E[\\hat{n}]$} & \\multicolumn{2}{c}{$E[\\hat{n}]/n$} \\\\ \\hline \\hline\n');
S='';
for k=1:N
    S=[S sprintf('%d & %8.2f & %8.4f \\\\ \n', n_i(k),E_n_stima(k),ratio(k))];
end

S = regexprep(S,'\.', '&');
disp(S);

s = regexprep('str', 'expr', 'repstr');



disp(' ');
disp('(Expected value)');
disp('Latex Output:');
disp(' ');
%output for latex, 5 column, decimal dot alignment
S='';
for k=1:N
    S=[S sprintf('%d & %8.2f & %8.4f \\\\ \n', FINE(k,:))];
end

S = regexprep(S,'\.', '&');
disp(S);

% not necessary if using \usepackage{dcolumn}
%s = regexprep('str', 'expr', 'repstr');

fid = fopen('latex_greenberg_basic_stop.txt', 'w');


disp(' ');
disp('(Stop Probability)');
disp('Latex Output:');
disp(' ');

[rows,cols]=size(FINE);

fprintf('\\begin{tabular}{');
for i=1:cols+1
    fprintf('c');
end
fprintf('}\n');

format short g;

fprintf('& ');
fprintf('%d &',n_i(2:end-1));
fprintf('%d \\\\ \n \\hline \\hline\n',n_i(end));

fprintf('& ');
fprintf('%d &',x_i(2:end-1));
fprintf('%d \\\\ \n \\hline \\hline\n',x_i(end));
for h=1:rows
        fprintf('%d &',n_i(h));
        for w=2:cols-1
            if (FINE(h, w)==0)
                fprintf('&');
            else
                fprintf(['%' formatme(FINE(h, w)) ' &'], FINE(h, w));
            end
        end
        fprintf(['%' formatme(FINE(h, cols)) '\\\\\\hline\r\n'], FINE(h, cols));
end
fprintf('\\end{tabular}\n');